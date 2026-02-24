package com.photondns.app.service

import android.util.Log
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.SwitchReason
import com.photondns.app.data.models.SwitchStrategy
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.LatencyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DNSSwitchManager @Inject constructor(
    private val dnsServerRepository: DNSServerRepository,
    private val latencyRepository: LatencyRepository,
    private val dnsLatencyChecker: DNSLatencyChecker
) {
    
    private val monitoringScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isMonitoring = AtomicBoolean(false)
    
    private val _currentStrategy = MutableStateFlow(SwitchStrategy.getPresets()[1]) // Balanced
    val currentStrategy: StateFlow<SwitchStrategy> = _currentStrategy.asStateFlow()
    
    private val _autoSwitchEnabled = MutableStateFlow(false)
    val autoSwitchEnabled: StateFlow<Boolean> = _autoSwitchEnabled.asStateFlow()
    
    private val _lastSwitchTime = MutableStateFlow(0L)
    val lastSwitchTime: StateFlow<Long> = _lastSwitchTime.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var consecutiveFastChecks = 0
    private var lastStableServer: String? = null
    
    companion object {
        private const val TAG = "DNSSwitchManager"
        private const val HYSTERESIS_THRESHOLD = 5 // ms
        private const val MIN_SWITCH_INTERVAL = 60000 // 1 minute minimum between switches
    }
    
    fun startAutoSwitching() {
        if (!isMonitoring.compareAndSet(false, true)) {
            return // Already monitoring
        }
        
        _autoSwitchEnabled.value = true
        monitoringJob = monitoringScope.launch {
            while (isMonitoring.get()) {
                try {
                    checkAndSwitchIfNeeded()
                    delay(_currentStrategy.value.checkInterval * 1000L)
                } catch (e: Exception) {
                    Log.e(TAG, "Auto-switching error", e)
                    delay(5000) // Wait 5 seconds before retry
                }
            }
        }
    }
    
    fun stopAutoSwitching() {
        isMonitoring.set(false)
        _autoSwitchEnabled.value = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    suspend fun checkAndSwitchIfNeeded() {
        if (!_autoSwitchEnabled.value) return
        
        val currentServer = dnsServerRepository.getActiveServer()
        if (currentServer == null) {
            Log.w(TAG, "No active DNS server found")
            return
        }
        
        val allServers = dnsServerRepository.getAllServersList()
        if (allServers.size < 2) return
        
        // Update latency for all servers
        updateServerLatencies(allServers)

        // Use a fresh snapshot after latency updates.
        val refreshedServers = dnsServerRepository.getAllServersList()
        val currentWithLatestLatency = refreshedServers.find { it.id == currentServer.id } ?: currentServer
        
        val fastestServer = refreshedServers
            .filter { it.latency > 0 }
            .minByOrNull { it.latency }
        
        if (fastestServer == null || fastestServer.id == currentWithLatestLatency.id) {
            // Current server is already the fastest
            consecutiveFastChecks = 0
            return
        }
        
        // Check if we should switch
        if (shouldSwitch(currentWithLatestLatency, fastestServer)) {
            performSwitch(currentWithLatestLatency, fastestServer, SwitchReason.AUTO_SWITCH)
        }
    }
    
    private suspend fun updateServerLatencies(servers: List<DNSServer>) {
        servers.forEach { server ->
            try {
                val latency = dnsLatencyChecker.checkLatency(server.ip)
                dnsServerRepository.updateServerLatency(server.id, latency)
                
                // Record latency in database
                latencyRepository.recordLatency(
                    serverId = server.id,
                    serverName = server.name,
                    serverIp = server.ip,
                    latency = latency,
                    success = latency > 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check latency for ${server.name}", e)
                latencyRepository.recordLatency(
                    serverId = server.id,
                    serverName = server.name,
                    serverIp = server.ip,
                    latency = -1,
                    success = false
                )
            }
        }
    }
    
    fun shouldSwitch(current: DNSServer, fastest: DNSServer): Boolean {
        val strategy = _currentStrategy.value
        
        // Don't switch if current server is failing and fastest is also failing
        if (current.latency <= 0 && fastest.latency <= 0) return false
        
        // Switch immediately if current server is failing
        if (current.latency <= 0 && fastest.latency > 0) return true
        
        // Check minimum time interval between switches
        val timeSinceLastSwitch = System.currentTimeMillis() - _lastSwitchTime.value
        if (timeSinceLastSwitch < MIN_SWITCH_INTERVAL) {
            return false
        }
        
        // Check improvement threshold
        val improvement = current.latency - fastest.latency
        if (improvement < strategy.minImprovement) return false
        
        // Apply hysteresis to prevent flip-flopping
        if (improvement < HYSTERESIS_THRESHOLD) return false
        
        // Check consecutive fast checks
        if (fastest.id == lastStableServer) {
            consecutiveFastChecks++
        } else {
            consecutiveFastChecks = 1
            lastStableServer = fastest.id
        }
        
        return consecutiveFastChecks >= strategy.consecutiveChecks
    }
    
    suspend fun performSwitch(
        fromServer: DNSServer,
        toServer: DNSServer,
        reason: SwitchReason
    ) {
        try {
            // Deactivate current server
            dnsServerRepository.setActiveServer(toServer.id)
            
            // Record the switch event
            latencyRepository.recordSwitchEvent(
                fromServerId = fromServer.id,
                fromServerName = fromServer.name,
                toServerId = toServer.id,
                toServerName = toServer.name,
                reason = reason,
                previousLatency = fromServer.latency,
                newLatency = toServer.latency
            )
            
            _lastSwitchTime.value = System.currentTimeMillis()
            consecutiveFastChecks = 0
            
            Log.i(TAG, "Switched DNS from ${fromServer.name} (${fromServer.latency}ms) to ${toServer.name} (${toServer.latency}ms)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch DNS server", e)
        }
    }
    
    suspend fun manualSwitch(serverId: String) {
        val currentServer = dnsServerRepository.getActiveServer()
        val targetServer = dnsServerRepository.getServerById(serverId)
        
        if (targetServer == null || currentServer?.id == serverId) return

        if (currentServer == null) {
            dnsServerRepository.setActiveServer(serverId)
            _lastSwitchTime.value = System.currentTimeMillis()
            return
        }

        if (currentServer.id != serverId) {
            performSwitch(
                fromServer = currentServer,
                toServer = targetServer,
                reason = SwitchReason.MANUAL_SWITCH
            )
        }
    }
    
    fun updateStrategy(strategy: SwitchStrategy) {
        _currentStrategy.value = strategy
        consecutiveFastChecks = 0 // Reset consecutive checks when strategy changes
    }
    
    fun setAutoSwitchEnabled(enabled: Boolean) {
        _autoSwitchEnabled.value = enabled
        if (enabled) {
            startAutoSwitching()
        } else {
            stopAutoSwitching()
        }
    }
    
    suspend fun getSwitchStatistics(): SwitchStatistics {
        val now = System.currentTimeMillis()
        val dayAgo = now - (24 * 60 * 60 * 1000)
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        val dailySwitches = latencyRepository.getSwitchCountSince(dayAgo)
        val weeklySwitches = latencyRepository.getSwitchCountSince(weekAgo)
        val lastSwitch = latencyRepository.getLatestSwitchEvent()
        
        return SwitchStatistics(
            dailySwitchCount = dailySwitches,
            weeklySwitchCount = weeklySwitches,
            lastSwitchTime = lastSwitch?.timestamp,
            lastSwitchReason = lastSwitch?.reason,
            currentStrategy = _currentStrategy.value.name,
            autoSwitchEnabled = _autoSwitchEnabled.value
        )
    }
    
    fun cleanup() {
        stopAutoSwitching()
        monitoringScope.cancel()
    }
}

data class SwitchStatistics(
    val dailySwitchCount: Int,
    val weeklySwitchCount: Int,
    val lastSwitchTime: Long?,
    val lastSwitchReason: SwitchReason?,
    val currentStrategy: String,
    val autoSwitchEnabled: Boolean
)
