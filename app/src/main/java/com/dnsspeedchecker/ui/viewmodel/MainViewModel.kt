package com.dnsspeedchecker.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsspeedchecker.DnsSpeedCheckerApplication
import com.dnsspeedchecker.data.preferences.DNSSettings
import com.dnsspeedchecker.data.preferences.DNSSettingsRepository
import com.dnsspeedchecker.data.preferences.Strategy
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.service.DnsLatencyChecker
import com.dnsspeedchecker.service.DnsMonitoringService
import com.dnsspeedchecker.service.DnsTestUtility
import com.dnsspeedchecker.service.DnsVpnService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isVpnConnected: Boolean = false,
    val currentDnsServer: DnsServer = DnsServer.DEFAULT_SERVERS[0],
    val dnsLatencies: Map<String, Long> = emptyMap(),
    val detailedDnsResults: Map<String, com.dnsspeedchecker.service.DnsLatencyResult> = emptyMap(),
    val isAutoSwitchEnabled: Boolean = false,
    val latencyHistory: List<Long> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPerformingHealthCheck: Boolean = false
)

data class SettingsState(
    val dnsSettings: DNSSettings = DNSSettings()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application
    private val dnsSettingsRepository = (application as DnsSpeedCheckerApplication).dnsSettingsRepository
    private val settingsRepository = (application as DnsSpeedCheckerApplication).settingsRepository
    private val dnsLatencyChecker = DnsLatencyChecker()
    private val dnsTestUtility = DnsTestUtility()
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
        observeSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dnsSettingsRepository.dnsSettingsFlow.collect { settings ->
                _settingsState.update { it.copy(dnsSettings = settings) }
                _uiState.update { it.copy(isAutoSwitchEnabled = settings.autoSwitchEnabled) }
            }
        }
    }
    
    private fun observeSettings() {
        // Combine DNS settings to update monitoring service
        viewModelScope.launch {
            dnsSettingsRepository.dnsSettingsFlow.collect { settings ->
                updateMonitoringServiceWithNewSettings(settings)
            }
        }
    }
    
    private fun updateMonitoringServiceWithNewSettings(settings: DNSSettings) {
        val intent = Intent(context, DnsMonitoringService::class.java).apply {
            action = "UPDATE_SETTINGS"
            putExtra("check_interval", settings.getEffectiveCheckInterval() * 1000L)
            putExtra("switching_threshold", settings.getEffectiveMinImprovement().toLong())
            putExtra("auto_switch_enabled", settings.autoSwitchEnabled)
            putExtra("consecutive_checks_needed", settings.getEffectiveConsecutiveChecks())
            putExtra("stability_period", settings.getEffectiveStabilityPeriod() * 60 * 1000L) // Convert to milliseconds
            putExtra("hysteresis_enabled", settings.hysteresisEnabled)
            putExtra("hysteresis_margin", settings.hysteresisMargin)
            putExtra("switch_on_failure", settings.switchOnFailure)
            putExtra("battery_saver_mode", settings.batterySaverMode)
        }
        context.startService(intent)
    }
    
    fun toggleVpn() {
        val currentState = _uiState.value
        if (currentState.isVpnConnected) {
            stopVpn()
        } else {
            startVpn()
        }
    }
    
    private fun startVpn() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            // VPN permission not granted - this should be handled by the UI
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = "VPN permission required"
                ) 
            }
        } else {
            // Permission already granted, start VPN
            context.startService(Intent(context, DnsVpnService::class.java).apply {
                action = "START_VPN"
            })
            
            context.startService(Intent(context, DnsMonitoringService::class.java).apply {
                action = "START_MONITORING"
            })
            
            _uiState.update { 
                it.copy(
                    isVpnConnected = true,
                    isLoading = false
                ) 
            }
        }
    }
    
    private fun stopVpn() {
        context.stopService(Intent(context, DnsVpnService::class.java))
        context.stopService(Intent(context, DnsMonitoringService::class.java))
        
        _uiState.update { 
            it.copy(
                isVpnConnected = false,
                dnsLatencies = emptyMap(),
                latencyHistory = emptyList()
            ) 
        }
    }
    
    fun toggleAutoSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSwitchEnabled(enabled)
        }
    }
    
    fun updateDnsLatency(serverId: String, latency: Long) {
        _uiState.update { currentState ->
            val newLatencies = currentState.dnsLatencies.toMutableMap()
            newLatencies[serverId] = latency
            
            val newHistory = if (latency > 0) {
                (currentState.latencyHistory + latency).takeLast(20)
            } else {
                currentState.latencyHistory
            }
            
            currentState.copy(
                dnsLatencies = newLatencies,
                latencyHistory = newHistory
            )
        }
    }
    
    fun updateDetailedDnsResult(serverId: String, result: com.dnsspeedchecker.service.DnsLatencyResult) {
        _uiState.update { currentState ->
            val newResults = currentState.detailedDnsResults.toMutableMap()
            newResults[serverId] = result
            
            currentState.copy(detailedDnsResults = newResults)
        }
    }
    
    /**
     * Performs a comprehensive DNS latency test for all enabled servers
     */
    fun performComprehensiveDnsTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val enabledServers = DnsServer.DEFAULT_SERVERS.filter { it.isEnabled }
                val results = mutableMapOf<String, com.dnsspeedchecker.service.DnsLatencyResult>()
                
                for (server in enabledServers) {
                    val result = dnsLatencyChecker.measureDnsLatency(server.primaryIp)
                    results[server.id] = result
                    
                    // Update simple latency for backward compatibility
                    updateDnsLatency(server.id, result.getPrimaryLatency())
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        detailedDnsResults = results,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "DNS test failed: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Performs DNS server health check
     */
    fun performDnsHealthCheck() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingHealthCheck = true) }
            
            try {
                val enabledServers = DnsServer.DEFAULT_SERVERS.filter { it.isEnabled }
                val dnsServerIps = enabledServers.map { it.primaryIp }
                
                val healthStatuses = dnsTestUtility.performHealthCheck(dnsServerIps)
                
                // Log health check results
                healthStatuses.forEach { status ->
                    val serverName = enabledServers.find { it.primaryIp == status.server }?.name ?: status.server
                    Log.i("DNSHealthCheck", "$serverName: Score=${status.score.toInt()}, Healthy=${status.isHealthy}")
                }
                
                _uiState.update { it.copy(isPerformingHealthCheck = false) }
                
            } catch (e: Exception) {
                Log.e("DNSHealthCheck", "Health check failed", e)
                _uiState.update { it.copy(isPerformingHealthCheck = false) }
            }
        }
    }
    
    /**
     * Validates a specific DNS server
     */
    fun validateDnsServer(dnsServer: String, onResult: (com.dnsspeedchecker.service.DnsValidationResult) -> Unit) {
        viewModelScope.launch {
            try {
                val result = dnsTestUtility.validateDnsServer(dnsServer)
                onResult(result)
            } catch (e: Exception) {
                Log.e("DNSValidation", "Validation failed for $dnsServer", e)
            }
        }
    }
    
    fun updateCurrentDnsServer(server: DnsServer) {
        _uiState.update { it.copy(currentDnsServer = server) }
    }
    
    private fun updateMonitoringService(checkInterval: Long, switchingThreshold: Long, autoSwitch: Boolean) {
        val intent = Intent(context, DnsMonitoringService::class.java).apply {
            action = "UPDATE_SETTINGS"
            putExtra("check_interval", checkInterval)
            putExtra("switching_threshold", switchingThreshold)
            putExtra("auto_switch_enabled", autoSwitch)
        }
        context.startService(intent)
    }
    
    /**
     * Updates DNS settings
     */
    fun updateDNSSettings(settings: DNSSettings) {
        viewModelScope.launch {
            dnsSettingsRepository.updateDNSSettings(settings)
        }
    }
    
    /**
     * Exports DNS settings to JSON
     */
    suspend fun exportDNSSettings(): String {
        return dnsSettingsRepository.exportSettings()
    }
    
    /**
     * Imports DNS settings from JSON
     */
    suspend fun importDNSSettings(jsonString: String): Result<DNSSettings> {
        return dnsSettingsRepository.importSettings(jsonString)
    }
    
    /**
     * Resets DNS settings to defaults
     */
    fun resetDNSSettingsToDefaults() {
        viewModelScope.launch {
            dnsSettingsRepository.resetToDefaults()
        }
    }
    
    /**
     * Updates auto-switch enabled setting
     */
    fun toggleAutoSwitch(enabled: Boolean) {
        viewModelScope.launch {
            dnsSettingsRepository.setAutoSwitchEnabled(enabled)
        }
    }
    
    /**
     * Updates switching strategy
     */
    fun updateStrategy(strategy: Strategy) {
        viewModelScope.launch {
            dnsSettingsRepository.setStrategy(strategy)
        }
    }
    
    /**
     * Updates custom check interval
     */
    fun updateCustomCheckInterval(interval: Int) {
        viewModelScope.launch {
            dnsSettingsRepository.setCustomCheckInterval(interval)
        }
    }
    
    /**
     * Updates custom minimum improvement
     */
    fun updateCustomMinImprovement(improvement: Int) {
        viewModelScope.launch {
            dnsSettingsRepository.setCustomMinImprovement(improvement)
        }
    }
    
    /**
     * Updates custom consecutive checks
     */
    fun updateCustomConsecutiveChecks(checks: Int) {
        viewModelScope.launch {
            dnsSettingsRepository.setCustomConsecutiveChecks(checks)
        }
    }
    
    /**
     * Updates custom stability period
     */
    fun updateCustomStabilityPeriod(period: Int) {
        viewModelScope.launch {
            dnsSettingsRepository.setCustomStabilityPeriod(period)
        }
    }
    
    /**
     * Updates hysteresis enabled setting
     */
    fun updateHysteresisEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dnsSettingsRepository.setHysteresisEnabled(enabled)
        }
    }
    
    /**
     * Updates hysteresis margin
     */
    fun updateHysteresisMargin(margin: Int) {
        viewModelScope.launch {
            dnsSettingsRepository.setHysteresisMargin(margin)
        }
    }
    
    /**
     * Updates switch on failure setting
     */
    fun updateSwitchOnFailure(enabled: Boolean) {
        viewModelScope.launch {
            dnsSettingsRepository.setSwitchOnFailure(enabled)
        }
    }
    
    /**
     * Updates battery saver mode setting
     */
    fun updateBatterySaverMode(enabled: Boolean) {
        viewModelScope.launch {
            dnsSettingsRepository.setBatterySaverMode(enabled)
        }
    }
    
    // Settings methods
    fun updateCheckInterval(interval: Long) {
        viewModelScope.launch {
            settingsRepository.setCheckInterval(interval)
        }
    }
    
    fun updateSwitchingThreshold(threshold: Long) {
        viewModelScope.launch {
            settingsRepository.setSwitchingThreshold(threshold)
        }
    }
    
    fun updateDnsServerEnabled(serverId: String, enabled: Boolean) {
        viewModelScope.launch {
            val currentServers = _settingsState.value.enabledDnsServers.toMutableSet()
            if (enabled) {
                currentServers.add(serverId)
            } else {
                currentServers.remove(serverId)
            }
            settingsRepository.setEnabledDnsServers(currentServers)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources if needed
    }
}
