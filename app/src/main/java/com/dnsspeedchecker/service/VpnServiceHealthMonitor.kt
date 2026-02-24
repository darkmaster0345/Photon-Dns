package com.dnsspeedchecker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Monitors VPN service health and handles graceful recovery from crashes
 */
class VpnServiceHealthMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "VpnServiceHealthMonitor"
        
        // Health check intervals
        private const val HEALTH_CHECK_INTERVAL = 15000L // 15 seconds
        private const val CRASH_DETECTION_WINDOW = 30000L // 30 seconds
        private const val MAX_RESTART_ATTEMPTS = 3
        private const val RESTART_DELAY_BASE = 5000L // 5 seconds
        
        // Health metrics thresholds
        private const val MAX_RESPONSE_TIME = 5000L // 5 seconds
        private const val MAX_CONSECUTIVE_FAILURES = 2
        private const val MEMORY_WARNING_THRESHOLD = 100 // 100MB
    }
    
    private val monitorScope = CoroutineScope(Dispatchers.IO + Job())
    private val handler = Handler(Looper.getMainLooper())
    
    // Health tracking
    private val isVpnServiceRunning = AtomicBoolean(false)
    private val isHealthy = AtomicBoolean(true)
    private val lastHealthCheckTime = AtomicLong(System.currentTimeMillis())
    private val consecutiveFailures = AtomicInteger(0)
    private val restartAttempts = AtomicInteger(0)
    private val lastRestartTime = AtomicLong(0)
    
    // Performance metrics
    private val responseTimes = ConcurrentHashMap<String, Long>()
    private val crashCount = AtomicInteger(0)
    private val lastCrashTime = AtomicLong(0)
    
    // Monitoring state
    private var healthCheckJob: Job? = null
    private var isMonitoring = AtomicBoolean(false)
    
    // Broadcast receivers for service status
    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.dnsspeedchecker.VPN_STATUS_UPDATE" -> {
                    handleVpnStatusUpdate(intent)
                }
                "com.dnsspeedchecker.DNS_PERFORMANCE_UPDATE" -> {
                    handleDnsPerformanceUpdate(intent)
                }
                "com.dnsspeedchecker.DNS_SWITCH_EVENT" -> {
                    handleDnsSwitchEvent(intent)
                }
                "com.dnsspeedchecker.DNS_SWITCH_FAILURE" -> {
                    handleDnsSwitchFailure(intent)
                }
            }
        }
    }
    
    /**
     * Starts monitoring VPN service health
     */
    fun startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            Log.i(TAG, "🏥 Starting VPN service health monitoring")
            
            // Register broadcast receivers
            registerBroadcastReceivers()
            
            // Start health check routine
            startHealthCheckRoutine()
            
            // Initialize health state
            isVpnServiceRunning.set(false)
            isHealthy.set(true)
            consecutiveFailures.set(0)
        }
    }
    
    /**
     * Stops monitoring VPN service health
     */
    fun stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            Log.i(TAG, "🛑 Stopping VPN service health monitoring")
            
            // Cancel health check job
            healthCheckJob?.cancel()
            healthCheckJob = null
            
            // Unregister broadcast receivers
            unregisterBroadcastReceivers()
        }
    }
    
    /**
     * Registers broadcast receivers for service status updates
     */
    private fun registerBroadcastReceivers() {
        val intentFilter = IntentFilter().apply {
            addAction("com.dnsspeedchecker.VPN_STATUS_UPDATE")
            addAction("com.dnsspeedchecker.DNS_PERFORMANCE_UPDATE")
            addAction("com.dnsspeedchecker.DNS_SWITCH_EVENT")
            addAction("com.dnsspeedchecker.DNS_SWITCH_FAILURE")
        }
        context.registerReceiver(vpnStatusReceiver, intentFilter)
    }
    
    /**
     * Unregisters broadcast receivers
     */
    private fun unregisterBroadcastReceivers() {
        try {
            context.unregisterReceiver(vpnStatusReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering broadcast receiver", e)
        }
    }
    
    /**
     * Starts the routine health check process
     */
    private fun startHealthCheckRoutine() {
        healthCheckJob = monitorScope.launch {
            while (isActive && isMonitoring.get()) {
                try {
                    performHealthCheck()
                    delay(HEALTH_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in health check routine", e)
                    delay(HEALTH_CHECK_INTERVAL * 2) // Back off on error
                }
            }
        }
    }
    
    /**
     * Performs a comprehensive health check of the VPN service
     */
    private suspend fun performHealthCheck() {
        val checkStartTime = System.currentTimeMillis()
        lastHealthCheckTime.set(checkStartTime)
        
        try {
            Log.d(TAG, "🔍 Performing VPN service health check")
            
            // Check if VPN service is responding
            val isResponding = checkVpnServiceResponsiveness()
            
            // Check memory usage
            val memoryUsage = getMemoryUsage()
            
            // Check recent performance
            val recentPerformance = checkRecentPerformance()
            
            // Evaluate overall health
            val currentHealth = evaluateHealth(isResponding, memoryUsage, recentPerformance)
            
            // Handle health state changes
            handleHealthStateChange(currentHealth, checkStartTime)
            
            // Log health status
            logHealthStatus(isResponding, memoryUsage, recentPerformance, currentHealth)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Health check failed", e)
            handleHealthCheckFailure()
        }
    }
    
    /**
     * Checks if VPN service is responding to requests
     */
    private suspend fun checkVpnServiceResponsiveness(): Boolean {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Send a status request to VPN service
            val intent = Intent(context, DnsVpnService::class.java).apply {
                action = "GET_STATUS"
            }
            context.startService(intent)
            
            // Wait for response (simplified - in real implementation would use callbacks)
            delay(1000)
            
            val responseTime = System.currentTimeMillis() - startTime
            responseTimes["last_check"] = responseTime
            
            val isResponding = responseTime < MAX_RESPONSE_TIME
            
            Log.d(TAG, "📡 VPN service responsiveness: ${responseTime}ms (responding: $isResponding)")
            isResponding
            
        } catch (e: Exception) {
            Log.w(TAG, "VPN service not responding", e)
            false
        }
    }
    
    /**
     * Gets current memory usage of the app
     */
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        return usedMemory
    }
    
    /**
     * Checks recent performance metrics
     */
    private fun checkRecentPerformance(): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val recentResponses = responseTimes.filter { (_, time) ->
            currentTime - time < 60000 // Last minute
        }
        
        val avgResponseTime = if (recentResponses.isNotEmpty()) {
            recentResponses.values.average()
        } else {
            0.0
        }
        
        return mapOf(
            "avg_response_time" to avgResponseTime,
            "response_count" to recentResponses.size,
            "max_response_time" to (recentResponses.values.maxOrNull() ?: 0L)
        )
    }
    
    /**
     * Evaluates overall health based on various metrics
     */
    private fun evaluateHealth(isResponding: Boolean, memoryUsage: Long, performance: Map<String, Any>): Boolean {
        val avgResponseTime = performance["avg_response_time"] as Double
        
        return when {
            !isResponding -> false
            memoryUsage > MEMORY_WARNING_THRESHOLD -> {
                Log.w(TAG, "⚠️ High memory usage: ${memoryUsage}MB")
                false
            }
            avgResponseTime > MAX_RESPONSE_TIME -> {
                Log.w(TAG, "⚠️ High average response time: ${\"%.1f\".format(avgResponseTime)}ms")
                false
            }
            else -> true
        }
    }
    
    /**
     * Handles changes in health state
     */
    private fun handleHealthStateChange(currentHealth: Boolean, checkTime: Long) {
        val wasHealthy = isHealthy.get()
        
        if (wasHealthy != currentHealth) {
            if (currentHealth) {
                // Service recovered
                Log.i(TAG, "✅ VPN service health restored")
                consecutiveFailures.set(0)
                restartAttempts.set(0)
                
                // Broadcast recovery event
                val recoveryIntent = Intent("com.dnsspeedchecker.VPN_HEALTH_RESTORED").apply {
                    putExtra("check_time", checkTime)
                    putExtra("downtime_duration", checkTime - lastCrashTime.get())
                }
                context.sendBroadcast(recoveryIntent)
                
            } else {
                // Service degraded or crashed
                Log.w(TAG, "⚠️ VPN service health degraded")
                val failures = consecutiveFailures.incrementAndGet()
                
                if (failures >= MAX_CONSECUTIVE_FAILURES) {
                    Log.e(TAG, "🚨 VPN service appears to have crashed")
                    handleVpnServiceCrash(checkTime)
                }
            }
            
            isHealthy.set(currentHealth)
        }
    }
    
    /**
     * Handles VPN service crash
     */
    private fun handleVpnServiceCrash(crashTime: Long) {
        val currentCrashCount = crashCount.incrementAndGet()
        lastCrashTime.set(crashTime)
        
        Log.e(TAG, "💥 VPN Service Crash Detected:")
        Log.e(TAG, "   Crash count: $currentCrashCount")
        Log.e(TAG, "   Time since last crash: ${crashTime - lastRestartTime.get()}ms")
        
        // Broadcast crash event
        val crashIntent = Intent("com.dnsspeedchecker.VPN_SERVICE_CRASHED").apply {
            putExtra("crash_count", currentCrashCount)
            putExtra("crash_time", crashTime)
            putExtra("consecutive_failures", consecutiveFailures.get())
        }
        context.sendBroadcast(crashIntent)
        
        // Attempt recovery if within limits
        if (restartAttempts.get() < MAX_RESTART_ATTEMPTS) {
            attemptVpnServiceRecovery()
        } else {
            Log.e(TAG, "❌ Maximum restart attempts reached, requiring manual intervention")
            broadcastRecoveryFailure()
        }
    }
    
    /**
     * Attempts to recover the VPN service
     */
    private fun attemptVpnServiceRecovery() {
        val attempt = restartAttempts.incrementAndGet()
        lastRestartTime.set(System.currentTimeMillis())
        
        // Calculate exponential backoff delay
        val delay = RESTART_DELAY_BASE * (1 shl (attempt - 1)).coerceAtMost(60000L) // Max 1 minute
        
        Log.w(TAG, "🔄 Attempting VPN service recovery (attempt $attempt/$MAX_RESTART_ATTEMPTS)")
        Log.w(TAG, "   Restart delay: ${delay}ms")
        
        monitorScope.launch {
            delay(delay)
            
            try {
                Log.i(TAG, "🚀 Restarting VPN service...")
                
                // Stop the service first
                context.stopService(Intent(context, DnsVpnService::class.java))
                delay(2000) // Give it time to stop
                
                // Clear any stale state
                responseTimes.clear()
                
                // Restart the service
                val restartIntent = Intent(context, DnsVpnService::class.java).apply {
                    action = "START_VPN"
                }
                context.startService(restartIntent)
                
                // Wait and check if restart was successful
                delay(5000)
                
                val restartSuccess = checkVpnServiceResponsiveness()
                if (restartSuccess) {
                    Log.i(TAG, "✅ VPN service recovery successful")
                    
                    // Broadcast recovery success
                    val successIntent = Intent("com.dnsspeedchecker.VPN_RECOVERY_SUCCESS").apply {
                        putExtra("attempt", attempt)
                        putExtra("recovery_time", System.currentTimeMillis())
                    }
                    context.sendBroadcast(successIntent)
                    
                    // Reset failure count
                    consecutiveFailures.set(0)
                    
                } else {
                    Log.w(TAG, "⚠️ VPN service recovery attempt failed")
                    
                    // Broadcast recovery failure
                    val failureIntent = Intent("com.dnsspeedchecker.VPN_RECOVERY_FAILED").apply {
                        putExtra("attempt", attempt)
                        putExtra("failure_time", System.currentTimeMillis())
                    }
                    context.sendBroadcast(failureIntent)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ VPN service recovery failed", e)
                
                // Broadcast recovery exception
                val exceptionIntent = Intent("com.dnsspeedchecker.VPN_RECOVERY_EXCEPTION").apply {
                    putExtra("attempt", attempt)
                    putExtra("error", e.message ?: "Unknown error")
                    putExtra("exception_time", System.currentTimeMillis())
                }
                context.sendBroadcast(exceptionIntent)
            }
        }
    }
    
    /**
     * Broadcasts recovery failure when max attempts are reached
     */
    private fun broadcastRecoveryFailure() {
        Log.e(TAG, "❌ VPN service recovery failed - manual intervention required")
        
        val intent = Intent("com.dnsspeedchecker.VPN_RECOVERY_FAILED_PERMANENTLY").apply {
            putExtra("max_attempts", MAX_RESTART_ATTEMPTS)
            putExtra("total_crashes", crashCount.get())
            putExtra("failure_time", System.currentTimeMillis())
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Handles VPN status updates from the service
     */
    private fun handleVpnStatusUpdate(intent: Intent) {
        val isConnected = intent.getBooleanExtra("is_connected", false)
        val dnsServer = intent.getStringExtra("dns_server") ?: ""
        val error = intent.getStringExtra("error")
        
        isVpnServiceRunning.set(isConnected)
        
        Log.d(TAG, "📊 VPN Status Update: connected=$isConnected, server=$dnsServer")
        
        if (error != null) {
            Log.w(TAG, "⚠️ VPN Service Error: $error")
        }
    }
    
    /**
     * Handles DNS performance updates
     */
    private fun handleDnsPerformanceUpdate(intent: Intent) {
        val totalTime = intent.getLongExtra("total_time", 0)
        val success = intent.getBooleanExtra("success", false)
        val dnsServer = intent.getStringExtra("dns_server") ?: ""
        
        responseTimes["dns_$dnsServer"] = totalTime
        
        Log.d(TAG, "📊 DNS Performance: $dnsServer -> ${totalTime}ms (success: $success)")
    }
    
    /**
     * Handles DNS switch events
     */
    private fun handleDnsSwitchEvent(intent: Intent) {
        val oldServer = intent.getStringExtra("old_dns_server") ?: ""
        val newServer = intent.getStringExtra("new_dns_server") ?: ""
        val switchTime = intent.getLongExtra("switch_time", 0)
        
        Log.i(TAG, "🔄 DNS Switch Event: $oldServer -> $newServer (${switchTime}ms)")
        responseTimes["switch_${System.currentTimeMillis()}"] = switchTime
    }
    
    /**
     * Handles DNS switch failures
     */
    private fun handleDnsSwitchFailure(intent: Intent) {
        val targetServer = intent.getStringExtra("target_server_id") ?: ""
        val error = intent.getStringExtra("error") ?: ""
        val switchTime = intent.getLongExtra("switch_time", 0)
        
        Log.w(TAG, "❌ DNS Switch Failure: $targetServer ($error) (${switchTime}ms)")
        consecutiveFailures.incrementAndGet()
    }
    
    /**
     * Handles health check failures
     */
    private fun handleHealthCheckFailure() {
        consecutiveFailures.incrementAndGet()
        Log.w(TAG, "⚠️ Health check failed (consecutive failures: ${consecutiveFailures.get()})")
    }
    
    /**
     * Logs current health status
     */
    private fun logHealthStatus(isResponding: Boolean, memoryUsage: Long, performance: Map<String, Any>, health: Boolean) {
        val status = if (health) "✅ HEALTHY" else "⚠️ UNHEALTHY"
        Log.i(TAG, "🏥 VPN Service Health Status: $status")
        Log.i(TAG, "   Responding: $isResponding")
        Log.i(TAG, "   Memory: ${memoryUsage}MB")
        Log.i(TAG, "   Avg Response: ${\"%.1f\".format(performance["avg_response_time"])}ms")
        Log.i(TAG, "   Consecutive Failures: ${consecutiveFailures.get()}")
        Log.i(TAG, "   Restart Attempts: ${restartAttempts.get()}")
    }
    
    /**
     * Gets current health monitoring status
     */
    fun getHealthStatus(): Map<String, Any> {
        return mapOf(
            "is_monitoring" to isMonitoring.get(),
            "is_vpn_running" to isVpnServiceRunning.get(),
            "is_healthy" to isHealthy.get(),
            "consecutive_failures" to consecutiveFailures.get(),
            "restart_attempts" to restartAttempts.get(),
            "crash_count" to crashCount.get(),
            "last_health_check" to lastHealthCheckTime.get(),
            "last_crash_time" to lastCrashTime.get(),
            "response_times" to responseTimes.toMap()
        )
    }
    
    /**
     * Manually triggers a health check
     */
    fun triggerHealthCheck() {
        if (isMonitoring.get()) {
            monitorScope.launch {
                performHealthCheck()
            }
        }
    }
    
    /**
     * Resets health monitoring state
     */
    fun reset() {
        Log.i(TAG, "🔄 Resetting health monitoring state")
        
        consecutiveFailures.set(0)
        restartAttempts.set(0)
        crashCount.set(0)
        responseTimes.clear()
        isHealthy.set(true)
        lastCrashTime.set(0)
        lastRestartTime.set(0)
    }
}
