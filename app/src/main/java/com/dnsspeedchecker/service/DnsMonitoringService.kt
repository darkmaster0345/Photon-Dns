package com.dnsspeedchecker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dnsspeedchecker.MainActivity
import com.dnsspeedchecker.R
import com.dnsspeedchecker.model.DnsLatencyResult
import com.dnsspeedchecker.model.DnsServer
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class DnsMonitoringService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private var notificationManager: NotificationManager? = null
    
    private val dnsLatencyChecker = DnsLatencyChecker()
    private val performanceAnalyzer = DnsPerformanceAnalyzer()
    
    private var currentDnsServer = "8.8.8.8"
    private var autoSwitchEnabled = false
    private var checkInterval = 5000L // 5 seconds
    private var switchingThreshold = 20L // 20ms
    
    private val dnsHistory = mutableMapOf<String, MutableList<Long>>()
    private val consecutiveBetterCount = mutableMapOf<String, Int>()
    private val dnsResults = mutableMapOf<String, com.dnsspeedchecker.service.DnsLatencyResult>()
    private val performanceMetrics = mutableMapOf<String, DnsPerformanceAnalyzer.PerformanceMetrics>()
    
    companion object {
        private const val TAG = "DnsMonitoringService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "dns_monitoring"
        private const val MAX_HISTORY_SIZE = 5
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            "START_MONITORING" -> {
                startMonitoring()
                START_STICKY
            }
            "STOP_MONITORING" -> {
                stopMonitoring()
                START_NOT_STICKY
            }
            "DNS_LATENCY_RESULT" -> {
                handleDnsLatencyResult(intent)
                START_STICKY
            }
            "UPDATE_SETTINGS" -> {
                updateSettings(intent)
                START_STICKY
            }
            else -> START_NOT_STICKY
        }
    }
    
    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkAllDnsServers()
                    delay(checkInterval)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                    delay(checkInterval) // Wait before retrying
                }
            }
        }
        
        Log.d(TAG, "DNS monitoring started")
    }
    
    private fun stopMonitoring() {
        monitoringJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "DNS monitoring stopped")
    }
    
    private suspend fun checkAllDnsServers() {
        val dnsServers = DnsServer.DEFAULT_SERVERS.filter { it.isEnabled }
        
        // Check all DNS servers concurrently for better performance
        val checkJobs = dnsServers.map { server ->
            async {
                val result = dnsLatencyChecker.measureDnsLatency(server.primaryIp)
                Pair(server, result)
            }
        }
        
        val results = checkJobs.awaitAll()
        
        // Process results
        for ((server, result) in results) {
            processDnsResult(server, result)
        }
    }
    
    private fun processDnsResult(server: DnsServer, result: com.dnsspeedchecker.service.DnsLatencyResult) {
        // Store the detailed result
        dnsResults[server.id] = result
        
        // Update history with primary latency value
        val primaryLatency = result.getPrimaryLatency()
        updateDnsHistory(server.id, primaryLatency)
        
        // Analyze performance metrics
        val history = dnsHistory[server.id] ?: emptyList()
        
        // Update notification for current DNS server
        if (server.primaryIp == currentDnsServer) {
            updateNotification(server.name, primaryLatency, result.getStatus())
        }
        
        // Check if we should switch DNS servers
        if (autoSwitchEnabled && result.success) {
            checkForIntelligentDnsSwitch(server.id, history, result.successRate)
        }
        
        // Log detailed results for debugging
        if (result.success) {
            Log.d(TAG, "DNS ${server.name} (${server.primaryIp}): ${result.getStatus()}")
        } else {
            Log.w(TAG, "DNS ${server.name} (${server.primaryIp}) failed: ${result.error}")
        }
    }
    
    private fun updateDnsHistory(serverId: String, latency: Long) {
        val history = dnsHistory.getOrPut(serverId) { mutableListOf() }
        
        if (latency > 0) { // Only add successful measurements
            history.add(latency)
            if (history.size > MAX_HISTORY_SIZE) {
                history.removeAt(0)
            }
        }
    }
    
    private fun checkForIntelligentDnsSwitch(serverId: String, history: List<Long>, successRate: Float) {
        val currentHistory = dnsHistory[currentDnsServer] ?: return
        if (currentHistory.isEmpty()) return
        
        serviceScope.launch {
            try {
                // Analyze current and new server performance
                val currentMetrics = performanceAnalyzer.analyzePerformance(currentHistory, 1.0f)
                val newMetrics = performanceAnalyzer.analyzePerformance(history, successRate)
                
                // Get switch recommendation
                val recommendation = performanceAnalyzer.shouldSwitch(
                    currentMetrics, newMetrics, switchingThreshold
                )
                
                if (recommendation.shouldSwitch) {
                    val count = consecutiveBetterCount.getOrPut(serverId) { 0 } + 1
                    consecutiveBetterCount[serverId] = count
                    
                    if (count >= 3) { // 3 consecutive recommendations
                        switchDnsServer(serverId)
                        consecutiveBetterCount.clear()
                        Log.i(TAG, "Intelligent DNS switch: ${recommendation.reason} (confidence: ${(recommendation.confidence * 100).toInt()}%)")
                    }
                } else {
                    consecutiveBetterCount[serverId] = 0
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in intelligent DNS analysis", e)
                // Fallback to simple switching logic
                checkForDnsSwitch(serverId, history.lastOrNull() ?: -1L)
            }
        }
    }
    
    private fun checkForDnsSwitch(serverId: String, latency: Long) {
        if (latency <= 0) return // Skip failed measurements
        
        val currentHistory = dnsHistory[currentDnsServer] ?: return
        if (currentHistory.isEmpty()) return
        
        val currentAvgLatency = currentHistory.average()
        val improvement = currentAvgLatency - latency
        
        // Additional safety checks before switching
        val currentResult = dnsResults.values.find { it.serverIp == currentDnsServer }
        val newResult = dnsResults[serverId]
        
        val shouldSwitch = when {
            improvement >= switchingThreshold -> true
            currentResult?.success == false && newResult?.success == true -> true
            currentResult?.isFallback == true && newResult?.isFallback == false -> true
            else -> false
        }
        
        if (shouldSwitch) {
            val count = consecutiveBetterCount.getOrPut(serverId) { 0 } + 1
            consecutiveBetterCount[serverId] = count
            
            if (count >= 3) { // 3 consecutive better measurements
                switchDnsServer(serverId)
                consecutiveBetterCount.clear()
                Log.i(TAG, "Switching DNS server: improvement=${improvement}ms, new_server=${newResult?.serverIp}")
            }
        } else {
            consecutiveBetterCount[serverId] = 0
        }
    }
    
    private fun switchDnsServer(serverId: String) {
        val server = DnsServer.DEFAULT_SERVERS.find { it.id == serverId } ?: return
        currentDnsServer = server.primaryIp
        
        // Update VPN service
        Intent(this, DnsVpnService::class.java).also { intent ->
            intent.action = "CHANGE_DNS"
            intent.putExtra("dns_server", currentDnsServer)
            startService(intent)
        }
        
        Log.d(TAG, "Switched to DNS server: ${server.name} ($currentDnsServer)")
    }
    
    private fun handleDnsLatencyResult(intent: Intent) {
        val dnsServer = intent.getStringExtra("dns_server") ?: return
        val latency = intent.getLongExtra("latency", -1)
        val success = intent.getBooleanExtra("success", false)
        
        if (success && latency > 0) {
            val serverId = DnsServer.DEFAULT_SERVERS.find { it.primaryIp == dnsServer }?.id ?: return
            updateDnsHistory(serverId, latency)
        }
    }
    
    private fun updateSettings(intent: Intent) {
        autoSwitchEnabled = intent.getBooleanExtra("auto_switch_enabled", false)
        checkInterval = intent.getLongExtra("check_interval", 5000L)
        switchingThreshold = intent.getLongExtra("switching_threshold", 20L)
        
        // Restart monitoring with new settings
        if (monitoringJob?.isActive == true) {
            monitoringJob?.cancel()
            startMonitoring()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text, currentDnsServer, 0))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private fun updateNotification(dnsName: String, latency: Long, status: String) {
        val notificationText = if (latency > 0) {
            getString(R.string.notification_text, dnsName, latency)
        } else {
            "DNS: $dnsName | Status: $status"
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createPendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()
            
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }
}
