package com.dnsspeedchecker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dnsspeedchecker.DnsSpeedCheckerApplication
import com.dnsspeedchecker.R
import com.dnsspeedchecker.model.DnsLatencyResult
import com.dnsspeedchecker.model.DnsServer
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineScope

/**
 * Optimized DNS monitoring service with battery optimization and memory leak prevention
 */
class OptimizedDnsMonitoringService : Service() {
    
    // Use a single coroutine scope with proper cancellation
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    
    // Battery optimization
    private var powerManager: PowerManager? = null
    private var isInBackground = AtomicBoolean(false)
    private var lastUserInteraction = System.currentTimeMillis()
    
    // Memory optimization - use weak references where possible
    private var notificationManager: NotificationManager? = null
    
    // Network connectivity monitoring
    private lateinit var networkMonitor: NetworkConnectivityMonitor
    private var isNetworkConnected = true
    private var lastNetworkCheckTime = 0L
    
    // Slow network handling
    private lateinit var slowNetworkHandler: SlowNetworkHandler
    
    // VPN service health monitoring
    private lateinit var vpnHealthMonitor: VpnServiceHealthMonitor
    
    // Optimized data structures
    private val dnsLatencyChecker = DnsLatencyChecker()
    private val performanceAnalyzer = DnsPerformanceAnalyzer()
    
    // Configuration with battery optimization defaults
    private var currentDnsServer = "8.8.8.8"
    private var autoSwitchEnabled = false
    private var checkInterval = 10000L // Increased to 10 seconds for battery
    private var switchingThreshold = 20L
    private var adaptiveInterval = false
    
    // Enhanced switching logic configuration
    private var lastSwitchTime = 0L
    private val stabilityPeriod = 120000L // 2 minutes stability period
    private val highImprovementThreshold = 50L // 50ms+ faster
    private val mediumImprovementThreshold = 20L // 20-49ms faster
    private val highImprovementRequiredCount = 2 // Switch after 2 consecutive checks (10 seconds)
    private val mediumImprovementRequiredCount = 5 // Switch after 5 consecutive checks (25 seconds)
    
    // Efficient data storage with size limits
    private val dnsHistory = mutableMapOf<String, ConcurrentLinkedQueue<Long>>()
    private val consecutiveBetterCount = mutableMapOf<String, Int>()
    private val dnsResults = mutableMapOf<String, DnsLatencyResult>()
    private val performanceMetrics = mutableMapOf<String, DnsPerformanceAnalyzer.PerformanceMetrics>()
    
    // Battery optimization constants
    companion object {
        private const val TAG = "OptimizedDnsMonitoringService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "dns_monitoring"
        private const val MAX_HISTORY_SIZE = 3 // Reduced from 5 for memory
        private const val MAX_CONSECUTIVE_COUNT = 5 // Maximum consecutive checks for medium improvement
        private const val BACKGROUND_CHECK_INTERVAL = 30000L // 30 seconds when in background
        private const val FOREGROUND_CHECK_INTERVAL = 10000L // 10 seconds when in foreground
        private const val IDLE_TIMEOUT = 60000L // 1 minute of inactivity before reducing checks
        private const val MEMORY_CLEANUP_INTERVAL = 120000L // 2 minutes
        private const val MAX_MEMORY_USAGE_MB = 50 // Maximum memory usage threshold
        private const val NETWORK_CHECK_INTERVAL = 30000L // Check network every 30 seconds
        private const val NETWORK_TIMEOUT_THRESHOLD = 60000L // Consider disconnected after 60 seconds
        
        // Enhanced switching thresholds
        private const val HIGH_IMPROVEMENT_THRESHOLD = 50L // 50ms+ faster
        private const val MEDIUM_IMPROVEMENT_THRESHOLD = 20L // 20-49ms faster
        private const val STABILITY_PERIOD_MS = 120000L // 2 minutes
        private const val HIGH_IMPROVEMENT_REQUIRED_COUNT = 2
        private const val MEDIUM_IMPROVEMENT_REQUIRED_COUNT = 5
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize battery optimization
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Initialize network connectivity monitoring
        networkMonitor = NetworkConnectivityMonitor(this)
        
        // Initialize slow network handler
        slowNetworkHandler = SlowNetworkHandler(this)
        
        // Initialize VPN health monitor
        vpnHealthMonitor = VpnServiceHealthMonitor(this)
        
        createNotificationChannel()
        
        // Start memory cleanup routine
        startMemoryCleanup()
        
        Log.d(TAG, "Optimized DNS monitoring service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            "START_MONITORING" -> {
                startOptimizedMonitoring()
                START_STICKY
            }
            "STOP_MONITORING" -> {
                stopOptimizedMonitoring()
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
            "USER_INTERACTION" -> {
                onUserInteraction()
                START_STICKY
            }
            else -> START_NOT_STICKY
        }
    }
    
    private fun startOptimizedMonitoring() {
        if (monitoringJob?.isActive == true) {
            Log.w(TAG, "Monitoring is already running")
            return
        }
        
        try {
            startForeground(NOTIFICATION_ID, createOptimizedNotification())
            
            // Start network connectivity monitoring
            networkMonitor.startMonitoring()
            
            // Start VPN health monitoring
            vpnHealthMonitor.startMonitoring()
            
            monitoringJob = serviceScope.launch {
                isInBackground.set(false)
                lastUserInteraction = System.currentTimeMillis()
                
                while (isActive) {
                    try {
                        // Check network connectivity first
                        checkNetworkConnectivity()
                        
                        if (!isNetworkConnected) {
                            Log.w(TAG, "🌐 Network disconnected, pausing DNS monitoring")
                            delay(getAdaptiveCheckInterval())
                            continue
                        }
                        
                        // Adaptive interval based on battery and usage
                        val currentInterval = getAdaptiveCheckInterval()
                        
                        // Check memory usage before proceeding
                        if (isMemoryUsageHigh()) {
                            Log.w(TAG, "High memory usage detected, skipping this check")
                            delay(currentInterval * 2) // Double the interval
                            continue
                        }
                        
                        // Perform optimized DNS checks
                        performOptimizedDnsChecks()
                        
                        // Adaptive delay based on conditions
                        delay(currentInterval)
                        
                        // Clean up old data periodically
                        cleanupOldData()
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in monitoring loop", e)
                        // Exponential backoff on error
                        delay(getAdaptiveCheckInterval() * 2)
                    }
                }
            }
            
            Log.i(TAG, "Optimized DNS monitoring started")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start monitoring", e)
            stopSelf()
        }
    }
    
    private fun stopOptimizedMonitoring() {
        monitoringJob?.cancel()
        isInBackground.set(false)
        
        try {
            // Stop network monitoring
            networkMonitor.stopMonitoring()
            
            // Clean up slow network handler
            slowNetworkHandler.cleanup()
            
            // Stop VPN health monitoring
            vpnHealthMonitor.stopMonitoring()
            
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.i(TAG, "Optimized DNS monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping monitoring", e)
        }
    }
    
    /**
     * Checks network connectivity and updates service state
     */
    private suspend fun checkNetworkConnectivity() {
        val currentTime = System.currentTimeMillis()
        
        // Only check network status at intervals
        if (currentTime - lastNetworkCheckTime < NETWORK_CHECK_INTERVAL) {
            return
        }
        
        lastNetworkCheckTime = currentTime
        val wasConnected = isNetworkConnected
        
        try {
            // Use network monitor for quick check
            isNetworkConnected = networkMonitor.isConnected.value
            
            // Perform actual connectivity test if monitor says we're connected
            if (isNetworkConnected) {
                val testResult = networkMonitor.testConnectivity()
                isNetworkConnected = testResult
                
                if (!testResult) {
                    Log.w(TAG, "🌐 Network monitor says connected but test failed - possible network issues")
                }
            }
            
            // Log network status changes
            if (wasConnected != isNetworkConnected) {
                if (isNetworkConnected) {
                    Log.i(TAG, "✅ Network connectivity restored")
                    Log.i(TAG, "   Network info: ${networkMonitor.getNetworkInfo()}")
                    
                    // Broadcast network restoration event
                    val restoreEvent = Intent("com.dnsspeedchecker.NETWORK_RESTORED").apply {
                        putExtra("network_type", networkMonitor.networkType.value.name)
                        putExtra("connection_quality", networkMonitor.connectionQuality.value.name)
                    }
                    sendBroadcast(restoreEvent)
                    
                    // Clear any error states
                    consecutiveBetterCount.clear()
                    
                } else {
                    Log.w(TAG, "❌ Network connectivity lost")
                    Log.w(TAG, "   Network info: ${networkMonitor.getNetworkInfo()}")
                    
                    // Broadcast network loss event
                    val lossEvent = Intent("com.dnsspeedchecker.NETWORK_LOST").apply {
                        putExtra("downtime_start", currentTime)
                    }
                    sendBroadcast(lossEvent)
                    
                    // Update notification to show disconnected status
                    updateNotificationForNetworkLoss()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking network connectivity", e)
            // Assume disconnected on error to be safe
            isNetworkConnected = false
        }
    }
    
    /**
     * Updates notification to show network loss status
     */
    private fun updateNotificationForNetworkLoss() {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText("Network disconnected - DNS monitoring paused")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(android.graphics.Color.RED)
                .build()
            
            notificationManager?.notify(NOTIFICATION_ID, notification)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification for network loss", e)
        }
    }
    
    /**
     * Adaptive check interval based on battery level and usage patterns
     */
    private fun getAdaptiveCheckInterval(): Long {
        val baseInterval = if (isInBackground.get()) {
            BACKGROUND_CHECK_INTERVAL
        } else {
            FOREGROUND_CHECK_INTERVAL
        }
        
        // Further optimize if device is in power save mode
        if (powerManager?.isPowerSaveMode == true) {
            return baseInterval * 2
        }
        
        // Reduce frequency if user hasn't interacted recently
        val timeSinceInteraction = System.currentTimeMillis() - lastUserInteraction
        if (timeSinceInteraction > IDLE_TIMEOUT) {
            return baseInterval * 3
        }
        
        return baseInterval
    }
    
    /**
     * Optimized DNS checks with network disconnection handling
     */
    private suspend fun performOptimizedDnsChecks() {
        if (!isNetworkConnected) {
            Log.d(TAG, "🌐 Skipping DNS checks - network disconnected")
            return
        }
        
        val enabledServers = DnsServer.DEFAULT_SERVERS.filter { it.isEnabled }
        
        if (enabledServers.isEmpty()) {
            Log.w(TAG, "⚠️ No enabled DNS servers to check")
            return
        }
        
        Log.d(TAG, "🔍 Starting DNS checks for ${enabledServers.size} servers")
        
        // Batch DNS checks to reduce network overhead
        val checkJobs = enabledServers.map { server ->
            async {
                try {
                    val result = dnsLatencyChecker.measureDnsLatency(server.primaryIp)
                    
                    // Check if network disconnected during this test
                    if (!isNetworkConnected) {
                        Log.w(TAG, "🌐 Network disconnected during DNS test for ${server.name}")
                        return@async null
                    }
                    
                    processDnsResult(server.id, result)
                    
                } catch (e: Exception) {
                    // Check if this is a network-related error
                    if (isNetworkRelatedError(e)) {
                        Log.w(TAG, "🌐 Network error during DNS test for ${server.name}: ${e.message}")
                        // Mark network as disconnected to prevent further failed attempts
                        isNetworkConnected = false
                        return@async null
                    } else {
                        Log.e(TAG, "❌ Error checking DNS for ${server.name}", e)
                        return@async null
                    }
                }
            }
        }
        
        // Wait for all checks to complete with timeout
        try {
            withTimeout(10000) { // Increased timeout for network issues
                val results = checkJobs.awaitAll()
                val successfulChecks = results.count { it != null }
                
                Log.d(TAG, "📊 DNS checks completed: $successfulChecks/${enabledServers.size} successful")
                
                // Process results through slow network handler
                val allResults = mutableMapOf<String, DnsLatencyResult>()
                enabledServers.forEach { server ->
                    dnsResults[server.id]?.let { result ->
                        allResults[server.id] = result
                    }
                }
                
                if (allResults.isNotEmpty()) {
                    slowNetworkHandler.processDnsLatencyResults(allResults)
                }
                
                // If most checks failed, likely a network issue
                if (successfulChecks < enabledServers.size / 2) {
                    Log.w(TAG, "⚠️ High failure rate detected, possible network issues")
                    // Perform network connectivity test
                    val networkTest = networkMonitor.testConnectivity()
                    if (!networkTest) {
                        Log.w(TAG, "🌐 Network connectivity test failed, marking as disconnected")
                        isNetworkConnected = false
                    }
                }
                
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "⏰ DNS checks timed out, possible network issues")
            checkJobs.forEach { it.cancel() }
            
            // Mark network as potentially disconnected
            isNetworkConnected = false
        }
    }
    
    /**
     * Determines if an exception is network-related
     */
    private fun isNetworkRelatedError(e: Exception): Boolean {
        val errorMessage = e.message?.lowercase() ?: return false
        val exceptionClass = e.javaClass.simpleName.lowercase()
        
        return errorMessage.contains("network") ||
               errorMessage.contains("host") ||
               errorMessage.contains("connection") ||
               errorMessage.contains("timeout") ||
               errorMessage.contains("unreachable") ||
               exceptionClass.contains("network") ||
               exceptionClass.contains("socket") ||
               exceptionClass.contains("timeout") ||
               e is java.net.UnknownHostException ||
               e is java.net.ConnectException ||
               e is java.net.SocketTimeoutException ||
               e is java.net.NoRouteToHostException
    }
    
    /**
     * Process DNS result with memory optimization and detailed logging
     */
    private fun processDnsResult(serverId: String, result: DnsLatencyResult?) {
        if (result == null) {
            Log.w(TAG, "⚠️ Received null DNS result for $serverId")
            return
        }
        
        try {
            val serverName = DnsServer.DEFAULT_SERVERS.find { it.id == serverId }?.name ?: serverId
            val latency = result.getPrimaryLatency()
            
            Log.d(TAG, "📊 Processing DNS result for $serverName ($serverId):")
            Log.d(TAG, "   Latency: ${latency}ms (avg=${result.avgLatencyMs}ms, median=${result.medianLatencyMs}ms)")
            Log.d(TAG, "   Success: ${result.success} (rate=${"%.1f".format(result.successRate * 100)}%)")
            Log.d(TAG, "   Quality: ${result.getQuality()}")
            if (result.variance > 0) {
                Log.d(TAG, "   Variance: ${"%.2f".format(result.variance)}ms²")
            }
            
            // Store result with memory management
            dnsResults[serverId] = result
            
            // Update history with size limit
            val history = dnsHistory.getOrPut(serverId) { ConcurrentLinkedQueue() }
            
            if (latency > 0) {
                history.offer(latency)
                // Keep only recent items
                while (history.size > MAX_HISTORY_SIZE) {
                    val removed = history.poll()
                    Log.d(TAG, "🗑️ Removed old latency measurement: ${removed}ms")
                }
                
                Log.d(TAG, "📈 Updated history for $serverName: ${history.size}/$MAX_HISTORY_SIZE measurements")
                Log.d(TAG, "   Recent latencies: ${history.joinToString(", ") { "${it}ms" }}")
            } else {
                Log.w(TAG, "⚠️ Invalid latency for $serverName: $latency - not adding to history")
            }
            
            // Update performance metrics periodically (not on every check)
            if (history.size >= MAX_HISTORY_SIZE) {
                val metrics = performanceAnalyzer.analyzePerformance(
                    history.toList(), 
                    result.successRate
                )
                performanceMetrics[serverId] = metrics
                
                Log.d(TAG, "📊 Performance metrics for $serverName:")
                Log.d(TAG, "   Stability: ${"%.2f".format(metrics.stability)}")
                Log.d(TAG, "   Consistency: ${"%.2f".format(metrics.consistency)}")
                Log.d(TAG, "   Reliability: ${"%.2f".format(metrics.reliability)}")
            }
            
            // Check for intelligent switching
            if (autoSwitchEnabled && result.success) {
                Log.d(TAG, "🔄 Auto-switch enabled, checking for switch opportunity...")
                checkForIntelligentSwitch(serverId, latency)
            } else {
                if (!autoSwitchEnabled) {
                    Log.d(TAG, "⏸️ Auto-switch disabled, skipping switch check")
                } else {
                    Log.w(TAG, "⚠️ DNS test failed for $serverName, skipping switch check")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing DNS result for $serverId", e)
        }
    }
    
    /**
     * Enhanced intelligent switching with specific thresholds and stability period
     */
    private fun checkForIntelligentSwitch(serverId: String, latency: Long) {
        if (latency <= 0) {
            Log.d(TAG, "🔍 Skipping switch check for $serverId: invalid latency ($latency)")
            return
        }
        
        try {
            val currentHistory = dnsHistory[currentDnsServer]
            if (currentHistory == null || currentHistory.size < 2) {
                Log.d(TAG, "🔍 Skipping switch check for $serverId: insufficient current data (${currentHistory?.size ?: 0} measurements)")
                return
            }
            
            val currentLatencies = currentHistory.toList()
            val currentAvgLatency = currentLatencies.average()
            val currentMedianLatency = currentLatencies.sorted()[currentLatencies.size / 2]
            val improvement = currentAvgLatency - latency
            
            Log.d(TAG, "🔄 Enhanced Switch Analysis for $serverId:")
            Log.d(TAG, "   Current DNS ($currentDnsServer): avg=${"%.1f".format(currentAvgLatency)}ms, median=${currentMedianLatency}ms, samples=${currentLatencies.size}")
            Log.d(TAG, "   Candidate DNS ($serverId): ${latency}ms")
            Log.d(TAG, "   Improvement: ${"%.1f".format(improvement)}ms")
            
            // Check stability period (no switching for 2 minutes after last switch)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastSwitch = currentTime - lastSwitchTime
            val isInStabilityPeriod = timeSinceLastSwitch < stabilityPeriod
            
            if (isInStabilityPeriod) {
                Log.d(TAG, "⏸️ In stability period: ${timeSinceLastSwitch}ms/${stabilityPeriod}ms since last switch")
                Log.d(TAG, "   Remaining stability time: ${stabilityPeriod - timeSinceLastSwitch}ms")
                return
            }
            
            // Enhanced switching logic with specific thresholds
            val switchDecision = when {
                improvement >= highImprovementThreshold -> {
                    Log.d(TAG, "🚀 HIGH IMPROVEMENT: ${"%.1f".format(improvement)}ms >= ${highImprovementThreshold}ms")
                    Log.d(TAG, "   Required consecutive checks: $highImprovementRequiredCount (switch after ${highImprovementRequiredCount * checkInterval / 1000}s)")
                    SwitchDecision.HIGH_IMPROVEMENT
                }
                improvement >= mediumImprovementThreshold -> {
                    Log.d(TAG, "⚡ MEDIUM IMPROVEMENT: ${"%.1f".format(improvement)}ms >= ${mediumImprovementThreshold}ms")
                    Log.d(TAG, "   Required consecutive checks: $mediumImprovementRequiredCount (switch after ${mediumImprovementRequiredCount * checkInterval / 1000}s)")
                    SwitchDecision.MEDIUM_IMPROVEMENT
                }
                else -> {
                    Log.d(TAG, "📉 LOW IMPROVEMENT: ${"%.1f".format(improvement)}ms < ${mediumImprovementThreshold}ms")
                    Log.d(TAG, "   Not worth disruption - no switch")
                    SwitchDecision.LOW_IMPROVEMENT
                }
            }
            
            when (switchDecision) {
                SwitchDecision.HIGH_IMPROVEMENT -> {
                    handleSwitchDecision(serverId, improvement, highImprovementRequiredCount)
                }
                SwitchDecision.MEDIUM_IMPROVEMENT -> {
                    handleSwitchDecision(serverId, improvement, mediumImprovementRequiredCount)
                }
                SwitchDecision.LOW_IMPROVEMENT -> {
                    // Reset consecutive count for low improvement
                    val previousCount = consecutiveBetterCount[serverId] ?: 0
                    if (previousCount > 0) {
                        Log.d(TAG, "🔄 Resetting consecutive count for $serverId: $previousCount -> 0 (low improvement)")
                    }
                    consecutiveBetterCount[serverId] = 0
                }
            }
            
            // Log overall switching state
            val allCounts = consecutiveBetterCount.entries.joinToString(", ") { "${it.key}:${it.value}" }
            Log.d(TAG, "📊 Switch state: { $allCounts }")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in enhanced intelligent switching for $serverId", e)
        }
    }
    
    /**
     * Handles switch decision based on improvement level
     */
    private fun handleSwitchDecision(serverId: String, improvement: Double, requiredCount: Int) {
        val count = consecutiveBetterCount.getOrPut(serverId, 0) + 1
        consecutiveBetterCount[serverId] = count
        
        Log.d(TAG, "� Consecutive better count for $serverId: $count/$requiredCount")
        
        if (count >= requiredCount) {
            Log.i(TAG, "🚀 SWITCH TRIGGERED: $serverId consistently better than $currentDnsServer")
            Log.i(TAG, "   Performance improvement: ${"%.1f".format(improvement)}ms average")
            Log.i(TAG, "   Consistency: $count consecutive better measurements")
            Log.i(TAG, "   Switch decision: HIGH improvement" + if (requiredCount == highImprovementRequiredCount) "" else " (MEDIUM improvement)")
            
            switchDnsServer(serverId)
            consecutiveBetterCount.clear()
            lastSwitchTime = System.currentTimeMillis()
            
            // Log switch success metrics
            Log.i(TAG, "✅ Enhanced DNS Switch completed: $currentDnsServer -> $serverId")
            Log.i(TAG, "   Stability period activated: ${stabilityPeriod / 1000}s")
        } else {
            Log.d(TAG, "⏳ Waiting for consistency: $count/$requiredCount better measurements")
            Log.d(TAG, "   Estimated switch in: ${(requiredCount - count) * checkInterval / 1000}s")
        }
    }
    
    /**
     * Switch decision enum
     */
    private enum class SwitchDecision {
        HIGH_IMPROVEMENT,
        MEDIUM_IMPROVEMENT,
        LOW_IMPROVEMENT
    }
    
    /**
     * Switch DNS server with comprehensive error handling and logging
     */
    private fun switchDnsServer(serverId: String) {
        val switchStartTime = System.currentTimeMillis()
        
        try {
            val server = DnsServer.DEFAULT_SERVERS.find { it.id == serverId }
            if (server == null) {
                Log.e(TAG, "❌ Cannot switch: DNS server not found for ID: $serverId")
                return
            }
            
            val oldDnsServer = currentDnsServer
            val oldLatency = getLatestLatency(oldDnsServer)
            val newLatency = getLatestLatency(server.primaryIp)
            
            Log.i(TAG, "🔄 Initiating DNS switch:")
            Log.i(TAG, "   From: $oldDnsServer (${oldLatency}ms)")
            Log.i(TAG, "   To: ${server.name} (${server.primaryIp}) (${newLatency}ms)")
            
            currentDnsServer = server.primaryIp
            
            // Update VPN service
            val vpnIntent = Intent(this, DnsVpnService::class.java).apply {
                action = "CHANGE_DNS"
                putExtra("dns_server", currentDnsServer)
            }
            
            val vpnUpdateStartTime = System.currentTimeMillis()
            startService(vpnIntent)
            val vpnUpdateTime = System.currentTimeMillis() - vpnUpdateStartTime
            
            Log.d(TAG, "📡 VPN service update completed in ${vpnUpdateTime}ms")
            
            // Update notification
            updateNotification(server.name, newLatency)
            
            val totalSwitchTime = System.currentTimeMillis() - switchStartTime
            
            Log.i(TAG, "✅ DNS Switch completed successfully in ${totalSwitchTime}ms")
            
            // Broadcast switch event for UI updates
            val switchEvent = Intent("com.dnsspeedchecker.DNS_SWITCH_EVENT").apply {
                putExtra("old_dns_server", oldDnsServer)
                putExtra("new_dns_server", server.primaryIp)
                putExtra("new_dns_name", server.name)
                putExtra("old_latency", oldLatency)
                putExtra("new_latency", newLatency)
                putExtra("switch_time", totalSwitchTime)
                putExtra("improvement", if (oldLatency > 0 && newLatency > 0) oldLatency - newLatency else 0L)
                putExtra("stability_period_activated", true)
                putExtra("stability_period_duration", stabilityPeriod)
                putExtra("switch_decision", if (oldLatency - newLatency >= highImprovementThreshold) "HIGH_IMPROVEMENT" else "MEDIUM_IMPROVEMENT")
            }
            sendBroadcast(switchEvent)
            
        } catch (e: Exception) {
            val switchTime = System.currentTimeMillis() - switchStartTime
            Log.e(TAG, "❌ DNS Switch failed after ${switchTime}ms", e)
            
            // Broadcast switch failure event
            val failureEvent = Intent("com.dnsspeedchecker.DNS_SWITCH_FAILURE").apply {
                putExtra("target_server_id", serverId)
                putExtra("error", e.message ?: "Unknown error")
                putExtra("switch_time", switchTime)
            }
            sendBroadcast(failureEvent)
        }
    }
    
    /**
     * Memory usage monitoring
     */
    private fun isMemoryUsageHigh(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        return usedMemory > MAX_MEMORY_USAGE_MB
    }
    
    /**
     * Clean up old data to prevent memory leaks
     */
    private fun cleanupOldData() {
        try {
            // Clean up old DNS results
            val currentTime = System.currentTimeMillis()
            dnsResults.entries.removeIf { (_, result) ->
                currentTime - result.timestamp > 300000 // 5 minutes
            }
            
            // Limit history size
            dnsHistory.values.forEach { queue ->
                while (queue.size > MAX_HISTORY_SIZE) {
                    queue.poll()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old data", e)
        }
    }
    
    /**
     * Start memory cleanup routine
     */
    private fun startMemoryCleanup() {
        serviceScope.launch {
            while (isActive) {
                delay(MEMORY_CLEANUP_INTERVAL)
                
                try {
                    // Force garbage collection hints
                    System.gc()
                    
                    // Clean up old data
                    cleanupOldData()
                    
                    // Log memory usage
                    val runtime = Runtime.getRuntime()
                    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                    val maxMemory = runtime.maxMemory() / (1024 * 1024)
                    
                    if (usedMemory > MAX_MEMORY_USAGE_MB) {
                        Log.w(TAG, "High memory usage: ${usedMemory}MB / ${maxMemory}MB")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in memory cleanup", e)
                }
            }
        }
    }
    
    /**
     * Handle user interaction for adaptive behavior
     */
    private fun onUserInteraction() {
        lastUserInteraction = System.currentTimeMillis()
        isInBackground.set(false)
    }
    
    private fun handleDnsLatencyResult(intent: Intent) {
        val dnsServer = intent.getStringExtra("dns_server") ?: return
        val latency = intent.getLongExtra("latency", -1)
        val success = intent.getBooleanExtra("success", false)
        
        if (success && latency > 0) {
            val serverId = DnsServer.DEFAULT_SERVERS.find { it.primaryIp == dnsServer }?.id ?: return
            val result = DnsLatencyResult(
                serverIp = dnsServer,
                avgLatencyMs = latency,
                minLatencyMs = latency,
                maxLatencyMs = latency,
                medianLatencyMs = latency,
                successRate = 1.0f,
                timestamp = System.currentTimeMillis(),
                success = true
            )
            processDnsResult(serverId, result)
        }
    }
    
    private fun updateSettings(intent: Intent) {
        autoSwitchEnabled = intent.getBooleanExtra("auto_switch_enabled", false)
        checkInterval = intent.getLongExtra("check_interval", FOREGROUND_CHECK_INTERVAL)
        switchingThreshold = intent.getLongExtra("switching_threshold", 20L)
        adaptiveInterval = intent.getBooleanExtra("adaptive_interval", true)
        
        Log.d(TAG, "Settings updated: interval=${checkInterval}ms, threshold=${switchingThreshold}ms, adaptive=$adaptiveInterval")
    }
    
    private fun getLatestLatency(dnsServer: String): Long {
        val serverId = DnsServer.DEFAULT_SERVERS.find { it.primaryIp == dnsServer }?.id
        return serverId?.let { dnsResults[it]?.avgLatencyMs } ?: -1L
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
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun createOptimizedNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val currentLatency = getLatestLatency(currentDnsServer)
        val contentText = if (currentLatency > 0) {
            getString(R.string.notification_text, currentDnsServer, currentLatency)
        } else {
            "DNS: $currentDnsServer | Checking..."
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateNotification(dnsName: String, latency: Long) {
        val notification = createOptimizedNotification()
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Clean up when task is removed
        stopOptimizedMonitoring()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "System low memory detected, performing aggressive cleanup")
        
        // Aggressive cleanup
        cleanupOldData()
        dnsResults.clear()
        performanceMetrics.clear()
        
        // Reduce check frequency
        checkInterval = checkInterval * 2
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up resources
        monitoringJob?.cancel()
        serviceScope.cancel()
        
        // Clear data structures
        dnsHistory.clear()
        consecutiveBetterCount.clear()
        dnsResults.clear()
        performanceMetrics.clear()
        
        Log.d(TAG, "Optimized DNS monitoring service destroyed")
    }
    
    override fun onBind(intent: Intent?) = null
}
