package com.dnsspeedchecker.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dnsspeedchecker.model.DnsServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles scenarios when all DNS servers are slow or experiencing high latency
 */
class SlowNetworkHandler(private val context: Context) {
    
    companion object {
        private const val TAG = "SlowNetworkHandler"
        
        // Thresholds for detecting slow network conditions
        private const val SLOW_LATENCY_THRESHOLD = 500L // 500ms
        private const val VERY_SLOW_LATENCY_THRESHOLD = 1000L // 1 second
        private const val CRITICAL_LATENCY_THRESHOLD = 2000L // 2 seconds
        
        // Consecutive slow measurements before taking action
        private const val SLOW_COUNT_THRESHOLD = 3
        private const val VERY_SLOW_COUNT_THRESHOLD = 2
        private const val CRITICAL_COUNT_THRESHOLD = 1
        
        // Recovery check intervals
        private const val RECOVERY_CHECK_INTERVAL = 30000L // 30 seconds
        private const val EMERGENCY_CHECK_INTERVAL = 60000L // 1 minute
        
        // Maximum time to stay in slow mode
        private const val MAX_SLOW_MODE_DURATION = 300000L // 5 minutes
    }
    
    private val handlerScope = CoroutineScope(Dispatchers.IO + Job())
    private val isSlowModeActive = AtomicBoolean(false)
    private val slowModeStartTime = AtomicLong(0)
    private val recoveryCheckJob = AtomicReference<Job?>(null)
    
    // Track consecutive slow measurements per server
    private val slowMeasurementCounts = ConcurrentHashMap<String, Int>()
    private val lastSlowMeasurementTime = ConcurrentHashMap<String, Long>()
    
    // Track overall network performance
    private val overallSlowCount = AtomicLong(0)
    private val lastOverallCheckTime = AtomicLong(System.currentTimeMillis())
    
    enum class SlowNetworkLevel {
        NORMAL, SLOW, VERY_SLOW, CRITICAL
    }
    
    private var currentSlowLevel = SlowNetworkLevel.NORMAL
    
    /**
     * Processes DNS latency results and detects slow network conditions
     */
    fun processDnsLatencyResults(results: Map<String, DnsLatencyResult>) {
        if (results.isEmpty()) {
            Log.w(TAG, "No DNS results to process")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val successfulResults = results.values.filter { it.success }
        
        if (successfulResults.isEmpty()) {
            Log.w(TAG, "No successful DNS results - possible network issues")
            handleNoSuccessfulResults(currentTime)
            return
        }
        
        // Analyze latency distribution
        val latencies = successfulResults.map { it.getPrimaryLatency() }
        val avgLatency = latencies.average()
        val maxLatency = latencies.maxOrNull() ?: 0L
        val minLatency = latencies.minOrNull() ?: 0L
        
        Log.d(TAG, "📊 Network Performance Analysis:")
        Log.d(TAG, "   Average latency: ${\"%.1f\".format(avgLatency)}ms")
        Log.d(TAG, "   Min latency: ${minLatency}ms")
        Log.d(TAG, "   Max latency: ${maxLatency}ms")
        Log.d(TAG, "   Successful servers: ${successfulResults.size}/${results.size}")
        
        // Update per-server slow counts
        updateSlowMeasurementCounts(successfulResults, currentTime)
        
        // Determine overall network condition
        val networkLevel = determineNetworkLevel(avgLatency, maxLatency, successfulResults.size)
        handleNetworkCondition(networkLevel, avgLatency, maxLatency, currentTime)
    }
    
    /**
     * Updates consecutive slow measurement counts for each server
     */
    private fun updateSlowMeasurementCounts(results: List<DnsLatencyResult>, currentTime: Long) {
        results.forEach { result ->
            val serverId = result.serverIp
            val latency = result.getPrimaryLatency()
            
            when {
                latency >= CRITICAL_LATENCY_THRESHOLD -> {
                    val count = slowMeasurementCounts.getOrPut(serverId, 0) + 1
                    slowMeasurementCounts[serverId] = count
                    lastSlowMeasurementTime[serverId] = currentTime
                    
                    Log.w(TAG, "⚠️ Critical latency for $serverId: ${latency}ms (count: $count)")
                }
                latency >= VERY_SLOW_LATENCY_THRESHOLD -> {
                    val count = slowMeasurementCounts.getOrPut(serverId, 0) + 1
                    slowMeasurementCounts[serverId] = count
                    lastSlowMeasurementTime[serverId] = currentTime
                    
                    Log.w(TAG, "⚠️ Very slow latency for $serverId: ${latency}ms (count: $count)")
                }
                latency >= SLOW_LATENCY_THRESHOLD -> {
                    val count = slowMeasurementCounts.getOrPut(serverId, 0) + 1
                    slowMeasurementCounts[serverId] = count
                    lastSlowMeasurementTime[serverId] = currentTime
                    
                    Log.d(TAG, "🐌 Slow latency for $serverId: ${latency}ms (count: $count)")
                }
                else -> {
                    // Reset count if latency is good
                    val previousCount = slowMeasurementCounts.remove(serverId) ?: 0
                    if (previousCount > 0) {
                        Log.i(TAG, "✅ $serverId recovered from slow state (was $previousCount)")
                    }
                }
            }
        }
    }
    
    /**
     * Determines the overall network condition level
     */
    private fun determineNetworkLevel(avgLatency: Double, maxLatency: Long, successCount: Int): SlowNetworkLevel {
        return when {
            avgLatency >= CRITICAL_LATENCY_THRESHOLD || maxLatency >= CRITICAL_LATENCY_THRESHOLD * 2 -> {
                SlowNetworkLevel.CRITICAL
            }
            avgLatency >= VERY_SLOW_LATENCY_THRESHOLD || maxLatency >= VERY_SLOW_LATENCY_THRESHOLD * 2 -> {
                SlowNetworkLevel.VERY_SLOW
            }
            avgLatency >= SLOW_LATENCY_THRESHOLD || maxLatency >= SLOW_LATENCY_THRESHOLD * 2 -> {
                SlowNetworkLevel.SLOW
            }
            successCount < DnsServer.DEFAULT_SERVERS.size / 2 -> {
                SlowNetworkLevel.SLOW // Many servers failing
            }
            else -> SlowNetworkLevel.NORMAL
        }
    }
    
    /**
     * Handles different network conditions
     */
    private fun handleNetworkCondition(level: SlowNetworkLevel, avgLatency: Double, maxLatency: Long, currentTime: Long) {
        val previousLevel = currentSlowLevel
        currentSlowLevel = level
        
        when (level) {
            SlowNetworkLevel.CRITICAL -> {
                Log.e(TAG, "🚨 CRITICAL NETWORK CONDITION DETECTED")
                Log.e(TAG, "   Average: ${\"%.1f\".format(avgLatency)}ms, Max: ${maxLatency}ms")
                
                if (previousLevel != SlowNetworkLevel.CRITICAL) {
                    activateSlowMode(SlowNetworkLevel.CRITICAL, avgLatency, maxLatency)
                }
            }
            
            SlowNetworkLevel.VERY_SLOW -> {
                Log.w(TAG, "⚠️ VERY SLOW NETWORK CONDITION DETECTED")
                Log.w(TAG, "   Average: ${\"%.1f\".format(avgLatency)}ms, Max: ${maxLatency}ms")
                
                if (previousLevel == SlowNetworkLevel.NORMAL) {
                    activateSlowMode(SlowNetworkLevel.VERY_SLOW, avgLatency, maxLatency)
                }
            }
            
            SlowNetworkLevel.SLOW -> {
                Log.w(TAG, "🐌 SLOW NETWORK CONDITION DETECTED")
                Log.w(TAG, "   Average: ${\"%.1f\".format(avgLatency)}ms, Max: ${maxLatency}ms")
                
                if (previousLevel == SlowNetworkLevel.NORMAL) {
                    activateSlowMode(SlowNetworkLevel.SLOW, avgLatency, maxLatency)
                }
            }
            
            SlowNetworkLevel.NORMAL -> {
                if (previousLevel != SlowNetworkLevel.NORMAL) {
                    Log.i(TAG, "✅ Network performance returned to normal")
                    deactivateSlowMode()
                }
            }
        }
    }
    
    /**
     * Handles scenario when no DNS servers return successful results
     */
    private fun handleNoSuccessfulResults(currentTime: Long) {
        val consecutiveFailures = overallSlowCount.incrementAndGet()
        
        Log.w(TAG, "⚠️ No successful DNS results (consecutive failures: $consecutiveFailures)")
        
        when {
            consecutiveFailures >= CRITICAL_COUNT_THRESHOLD -> {
                activateSlowMode(SlowNetworkLevel.CRITICAL, 0.0, 0L)
            }
            consecutiveFailures >= VERY_SLOW_COUNT_THRESHOLD -> {
                activateSlowMode(SlowNetworkLevel.VERY_SLOW, 0.0, 0L)
            }
            consecutiveFailures >= SLOW_COUNT_THRESHOLD -> {
                activateSlowMode(SlowNetworkLevel.SLOW, 0.0, 0L)
            }
        }
    }
    
    /**
     * Activates slow mode with appropriate measures
     */
    private fun activateSlowMode(level: SlowNetworkLevel, avgLatency: Double, maxLatency: Long) {
        if (isSlowModeActive.compareAndSet(false, true)) {
            slowModeStartTime.set(System.currentTimeMillis())
            
            Log.w(TAG, "🐌 ACTIVATING SLOW MODE (Level: $level)")
            Log.w(TAG, "   Average latency: ${\"%.1f\".format(avgLatency)}ms")
            Log.w(TAG, "   Max latency: ${maxLatency}ms")
            
            // Broadcast slow mode activation
            val intent = Intent("com.dnsspeedchecker.SLOW_MODE_ACTIVATED").apply {
                putExtra("level", level.name)
                putExtra("avg_latency", avgLatency)
                putExtra("max_latency", maxLatency)
                putExtra("timestamp", System.currentTimeMillis())
            }
            context.sendBroadcast(intent)
            
            // Start recovery monitoring
            startRecoveryMonitoring(level)
            
            // Apply level-specific optimizations
            applySlowModeOptimizations(level)
        }
    }
    
    /**
     * Deactivates slow mode and returns to normal operation
     */
    private fun deactivateSlowMode() {
        if (isSlowModeActive.compareAndSet(true, false)) {
            val slowModeDuration = System.currentTimeMillis() - slowModeStartTime.get()
            
            Log.i(TAG, "✅ DEACTIVATING SLOW MODE")
            Log.i(TAG, "   Duration: ${slowModeDuration}ms")
            
            // Cancel recovery monitoring
            recoveryCheckJob.get()?.cancel()
            recoveryCheckJob.set(null)
            
            // Clear slow measurement counts
            slowMeasurementCounts.clear()
            lastSlowMeasurementTime.clear()
            overallSlowCount.set(0)
            
            // Broadcast slow mode deactivation
            val intent = Intent("com.dnsspeedchecker.SLOW_MODE_DEACTIVATED").apply {
                putExtra("duration", slowModeDuration)
                putExtra("timestamp", System.currentTimeMillis())
            }
            context.sendBroadcast(intent)
            
            // Restore normal operation settings
            restoreNormalOperation()
        }
    }
    
    /**
     * Starts recovery monitoring to detect when network conditions improve
     */
    private fun startRecoveryMonitoring(level: SlowNetworkLevel) {
        val checkInterval = when (level) {
            SlowNetworkLevel.CRITICAL -> EMERGENCY_CHECK_INTERVAL
            SlowNetworkLevel.VERY_SLOW -> EMERGENCY_CHECK_INTERVAL
            else -> RECOVERY_CHECK_INTERVAL
        }
        
        recoveryCheckJob.set(handlerScope.launch {
            while (isActive && isSlowModeActive.get()) {
                delay(checkInterval)
                
                try {
                    checkForRecovery()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during recovery check", e)
                }
            }
        })
        
        Log.d(TAG, "🔍 Started recovery monitoring (interval: ${checkInterval}ms)")
    }
    
    /**
     * Checks if network conditions have improved enough to exit slow mode
     */
    private suspend fun checkForRecovery() {
        try {
            // Test with a reliable DNS server
            val testResult = DnsLatencyChecker().measureDnsLatency("8.8.8.8")
            
            if (testResult.success) {
                val latency = testResult.getPrimaryLatency()
                
                Log.d(TAG, "🔍 Recovery test result: ${latency}ms")
                
                // Check if conditions have improved
                val canRecover = when (currentSlowLevel) {
                    SlowNetworkLevel.CRITICAL -> latency < VERY_SLOW_LATENCY_THRESHOLD
                    SlowNetworkLevel.VERY_SLOW -> latency < SLOW_LATENCY_THRESHOLD
                    SlowNetworkLevel.SLOW -> latency < SLOW_LATENCY_THRESHOLD / 2
                    else -> true
                }
                
                if (canRecover) {
                    Log.i(TAG, "✅ Network conditions improved, deactivating slow mode")
                    deactivateSlowMode()
                } else {
                    Log.d(TAG, "🐌 Network still slow, keeping slow mode active")
                }
            } else {
                Log.w(TAG, "⚠️ Recovery test failed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during recovery check", e)
        }
    }
    
    /**
     * Applies optimizations based on slow network level
     */
    private fun applySlowModeOptimizations(level: SlowNetworkLevel) {
        val optimizations = when (level) {
            SlowNetworkLevel.CRITICAL -> {
                listOf(
                    "Increase DNS check intervals to 60 seconds",
                    "Disable auto-switching to prevent flapping",
                    "Use only most reliable DNS servers",
                    "Reduce concurrent DNS tests to 1",
                    "Extend timeouts to 10 seconds"
                )
            }
            SlowNetworkLevel.VERY_SLOW -> {
                listOf(
                    "Increase DNS check intervals to 30 seconds",
                    "Reduce concurrent DNS tests to 2",
                    "Extend timeouts to 5 seconds",
                    "Prefer servers with lower variance"
                )
            }
            SlowNetworkLevel.SLOW -> {
                listOf(
                    "Increase DNS check intervals to 20 seconds",
                    "Extend timeouts to 3 seconds"
                )
            }
            else -> emptyList()
        }
        
        Log.i(TAG, "🔧 Applying slow mode optimizations:")
        optimizations.forEach { Log.i(TAG, "   - $it") }
        
        // Broadcast optimizations to monitoring service
        val intent = Intent("com.dnsspeedchecker.APPLY_SLOW_MODE_OPTIMIZATIONS").apply {
            putExtra("level", level.name)
            putStringArrayListExtra("optimizations", ArrayList(optimizations))
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Restores normal operation settings
     */
    private fun restoreNormalOperation() {
        Log.i(TAG, "🔄 Restoring normal operation settings")
        
        // Broadcast restoration to monitoring service
        val intent = Intent("com.dnsspeedchecker.RESTORE_NORMAL_OPERATION")
        context.sendBroadcast(intent)
    }
    
    /**
     * Gets current slow network status
     */
    fun getSlowNetworkStatus(): Map<String, Any> {
        return mapOf(
            "is_slow_mode_active" to isSlowModeActive.get(),
            "current_level" to currentSlowLevel.name,
            "slow_mode_duration" to if (isSlowModeActive.get()) System.currentTimeMillis() - slowModeStartTime.get() else 0L,
            "slow_measurement_counts" to slowMeasurementCounts.toMap(),
            "overall_slow_count" to overallSlowCount.get()
        )
    }
    
    /**
     * Cleans up resources
     */
    fun cleanup() {
        recoveryCheckJob.get()?.cancel()
        recoveryCheckJob.set(null)
        handlerScope.cancel()
    }
}
