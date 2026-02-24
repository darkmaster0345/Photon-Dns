package com.dnsspeedchecker.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.*
import com.dnsspeedchecker.DnsSpeedCheckerApplication
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.service.DnsLatencyChecker
import com.dnsspeedchecker.service.DnsMonitoringService
import com.dnsspeedchecker.service.DnsVpnService
import com.dnsspeedchecker.service.MemoryLeakPrevention
import com.dnsspeedchecker.service.OptimizedDnsMonitoringService
import com.dnsspeedchecker.utils.ApiVersionCompatibility
import com.dnsspeedchecker.utils.LogExporter
import com.dnsspeedchecker.utils.PermissionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Optimized MainViewModel with memory leak prevention and performance improvements
 */
class OptimizedMainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Use memory-safe references
    private val context = application
    private val settingsRepository = (application as DnsSpeedCheckerApplication).settingsRepository
    
    // Optimized services with proper cleanup
    private val dnsLatencyChecker = DnsLatencyChecker()
    private val memoryLeakPrevention = MemoryLeakPrevention
    
    // State flows with proper lifecycle management
    private val _uiState = MutableStateFlow(OptimizedMainUiState())
    val uiState: StateFlow<OptimizedMainUiState> = _uiState.asStateFlow()
    
    private val _settingsState = MutableStateFlow(OptimizedSettingsState())
    val settingsState: StateFlow<OptimizedSettingsState> = _settingsState.asStateFlow()
    
    // Background job management
    private var monitoringJob: Job? = null
    private var vpnPermissionJob: Job? = null
    
    // Performance optimization
    private var lastUpdateTime = 0L
    private val updateThrottleMs = 1000L // Throttle UI updates
    
    init {
        loadSettings()
        observeSettings()
        startMemoryMonitoring()
        logApiCompatibility()
    }
    
    data class OptimizedMainUiState(
        val isVpnConnected: Boolean = false,
        val currentDnsServer: DnsServer = DnsServer.DEFAULT_SERVERS[0],
        val dnsLatencies: Map<String, Long> = emptyMap(),
        val detailedDnsResults: Map<String, com.dnsspeedchecker.service.DnsLatencyResult> = emptyMap(),
        val isAutoSwitchEnabled: Boolean = false,
        val latencyHistory: List<Long> = emptyList(),
        val isLoading: Boolean = false,
        val isPerformingHealthCheck: Boolean = false,
        val error: String? = null,
        val lastUpdate: Long = System.currentTimeMillis(),
        val memoryUsage: Map<String, Any> = emptyMap(),
        val networkInfo: Map<String, Any> = emptyMap()
    )
    
    data class OptimizedSettingsState(
        val checkInterval: Long = 10000L,
        val switchingThreshold: Long = 20L,
        val enabledDnsServers: Set<String> = DnsServer.DEFAULT_SERVERS.map { it.id }.toSet(),
        val adaptiveBatteryMode: Boolean = true,
        val batteryOptimization: Boolean = true
    )
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                // Load settings with memory optimization
                settingsRepository.getCheckInterval().collect { interval ->
                    _settingsState.update { it.copy(checkInterval = interval) }
                }
                
                settingsRepository.getSwitchingThreshold().collect { threshold ->
                    _settingsState.update { it.copy(switchingThreshold = threshold) }
                }
                
                settingsRepository.getEnabledDnsServers().collect { servers ->
                    _settingsState.update { it.copy(enabledDnsServers = servers) }
                }
                
                settingsRepository.getAutoSwitchEnabled().collect { enabled ->
                    _uiState.update { it.copy(isAutoSwitchEnabled = enabled) }
                }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error loading settings", e)
            }
        }
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
            try {
                combine(
                    settingsState.map { it.checkInterval },
                    settingsState.map { it.switchingThreshold },
                    settingsState.map { it.adaptiveBatteryMode }
                ) { interval, threshold, adaptive ->
                    updateMonitoringService(interval, threshold, _uiState.value.isAutoSwitchEnabled)
                }
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error observing settings", e)
            }
        }
    }
    
    private fun startMemoryMonitoring() {
        memoryLeakPrevention.startMemoryMonitoring(viewModelScope)
    }
    
    private fun logApiCompatibility() {
        ApiVersionCompatibility.logCompatibilityInfo()
    }
    
    fun toggleVpn() {
        if (ApiVersionCompatibility.isVpnPermissionGranted(context)) {
            startVpnService()
        } else {
            requestVpnPermission()
        }
    }
    
    private fun startVpnService() {
        vpnPermissionJob?.cancel()
        
        vpnPermissionJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val intent = Intent(context, DnsVpnService::class.java).apply {
                    action = "START_VPN"
                }
                
                if (ApiVersionCompatibility.isAtLeast(ApiVersionCompatibility.API_26)) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                
                // Update state after a short delay to ensure service starts
                delay(1000)
                _uiState.update { it.copy(isLoading = false, isVpnConnected = true) }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error starting VPN service", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to start VPN: ${e.message}") }
            }
        }
    }
    
    private fun requestVpnPermission() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val vpnIntent = VpnService.prepare(context)
                if (vpnIntent != null) {
                    // Show permission rationale
                    _uiState.update { 
                        it.copy(isLoading = false, error = "VPN permission required") 
                    }
                } else {
                    // Permission already granted
                    startVpnService()
                }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error requesting VPN permission", e)
                _uiState.update { it.copy(isLoading = false, error = "Permission request failed: ${e.message}") }
            }
        }
    }
    
    fun stopVpn() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val intent = Intent(context, DnsVpnService::class.java).apply {
                    action = "STOP_VPN"
                }
                
                context.startService(intent)
                
                delay(500)
                _uiState.update { it.copy(isLoading = false, isVpnConnected = false) }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error stopping VPN service", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to stop VPN: ${e.message}") }
            }
        }
    }
    
    fun toggleAutoSwitch(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setAutoSwitchEnabled(enabled)
                _uiState.update { it.copy(isAutoSwitchEnabled = enabled) }
                
                // Update monitoring service
                val currentSettings = _settingsState.value
                updateMonitoringService(
                    currentSettings.checkInterval,
                    currentSettings.switchingThreshold,
                    enabled
                )
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error toggling auto switch", e)
            }
        }
    }
    
    private fun updateMonitoringService(checkInterval: Long, switchingThreshold: Long, autoSwitch: Boolean) {
        try {
            val intent = Intent(context, OptimizedDnsMonitoringService::class.java).apply {
                action = "UPDATE_SETTINGS"
                putExtra("check_interval", checkInterval)
                putExtra("switching_threshold", switchingThreshold)
                putExtra("auto_switch_enabled", autoSwitch)
            }
            
            context.startService(intent)
            
        } catch (e: Exception) {
            Log.e("OptimizedMainViewModel", "Error updating monitoring service", e)
        }
    }
    
    fun performComprehensiveDnsTest() {
        monitoringJob?.cancel()
        
        monitoringJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isPerformingHealthCheck = true, error = null) }
                
                val enabledServers = DnsServer.DEFAULT_SERVERS.filter { 
                    _settingsState.value.enabledDnsServers.contains(it.id) 
                }
                
                val results = mutableMapOf<String, com.dnsspeedchecker.service.DnsLatencyResult>()
                
                // Batch DNS checks with timeout
                val checkJobs = enabledServers.map { server ->
                    async {
                        try {
                            val result = dnsLatencyChecker.measureDnsLatency(server.primaryIp)
                            results[server.id] = result
                            
                            // Update UI with throttling
                            updateLatencyThrottled(server.id, result.getPrimaryLatency())
                            
                        } catch (e: Exception) {
                            Log.e("OptimizedMainViewModel", "Error testing ${server.name}", e)
                        }
                    }
                }
                
                // Wait for all checks with timeout
                withTimeout(15000) {
                    checkJobs.awaitAll()
                }
                
                _uiState.update { 
                    it.copy(
                        isPerformingHealthCheck = false,
                        detailedDnsResults = results,
                        lastUpdate = System.currentTimeMillis()
                    ) 
                }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error in comprehensive DNS test", e)
                _uiState.update { 
                    it.copy(
                        isPerformingHealthCheck = false, 
                        error = "DNS test failed: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun updateLatencyThrottled(serverId: String, latency: Long) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > updateThrottleMs) {
            lastUpdateTime = currentTime
            
            viewModelScope.launch {
                _uiState.update { currentState ->
                    val newLatencies = currentState.dnsLatencies.toMutableMap()
                    newLatencies[serverId] = latency
                    
                    val newHistory = (currentState.latencyHistory + latency).takeLast(20)
                    
                    currentState.copy(
                        dnsLatencies = newLatencies,
                        latencyHistory = newHistory,
                        lastUpdate = currentTime
                    )
                }
            }
        }
    }
    
    fun updateCurrentDnsServer(server: DnsServer) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(currentDnsServer = server) }
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error updating DNS server", e)
            }
        }
    }
    
    fun exportLogs(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                LogExporter.exportLogs(context) { success, message ->
                    _uiState.update { it.copy(isLoading = false) }
                    onResult(success, message)
                }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error exporting logs", e)
                _uiState.update { 
                    it.copy(isLoading = false, error = "Export failed: ${e.message}") 
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun refreshNetworkInfo() {
        viewModelScope.launch {
            try {
                val networkInfo = ApiVersionCompatibility.getNetworkInfo(context)
                val memoryStats = memoryLeakPrevention.getMemoryStats()
                
                _uiState.update { it.copy(
                    networkInfo = networkInfo,
                    memoryUsage = memoryStats,
                    lastUpdate = System.currentTimeMillis()
                ) }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error refreshing network info", e)
            }
        }
    }
    
    fun updateDnsServerEnabled(serverId: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentServers = _settingsState.value.enabledDnsServers.toMutableSet()
                
                if (enabled) {
                    currentServers.add(serverId)
                } else {
                    currentServers.remove(serverId)
                }
                
                settingsRepository.setEnabledDnsServers(currentServers)
                _settingsState.update { it.copy(enabledDnsServers = currentServers) }
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error updating DNS server enabled state", e)
            }
        }
    }
    
    fun updateCheckInterval(interval: Long) {
        viewModelScope.launch {
            try {
                settingsRepository.setCheckInterval(interval)
                _settingsState.update { it.copy(checkInterval = interval) }
                
                // Update monitoring service
                updateMonitoringService(interval, _settingsState.value.switchingThreshold, _uiState.value.isAutoSwitchEnabled)
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error updating check interval", e)
            }
        }
    }
    
    fun updateSwitchingThreshold(threshold: Long) {
        viewModelScope.launch {
            try {
                settingsRepository.setSwitchingThreshold(threshold)
                _settingsState.update { it.copy(switchingThreshold = threshold) }
                
                // Update monitoring service
                updateMonitoringService(_settingsState.value.checkInterval, threshold, _uiState.value.isAutoSwitchEnabled)
                
            } catch (e: Exception) {
                Log.e("OptimizedMainViewModel", "Error updating switching threshold", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Clean up resources
        monitoringJob?.cancel()
        vpnPermissionJob?.cancel()
        
        // Clear memory leak prevention resources
        memoryLeakPrevention.cleanup()
        
        Log.d("OptimizedMainViewModel", "ViewModel cleared and resources cleaned up")
    }
}
