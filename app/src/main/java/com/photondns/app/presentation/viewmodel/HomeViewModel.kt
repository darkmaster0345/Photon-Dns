package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.NetworkMetrics
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.LatencyRepository
import com.photondns.app.data.repository.SpeedTestRepository
import com.photondns.app.service.DNSLatencyChecker
import com.photondns.app.service.DNSSwitchManager
import com.photondns.app.service.SpeedTestManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dnsServerRepository: DNSServerRepository,
    private val latencyRepository: LatencyRepository,
    private val speedTestRepository: SpeedTestRepository,
    private val dnsLatencyChecker: DNSLatencyChecker,
    private val dnsSwitchManager: DNSSwitchManager,
    private val speedTestManager: SpeedTestManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observeData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                dnsServerRepository.ensureDefaultServersInitialized()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    private fun observeData() {
        viewModelScope.launch {
            combine(
                dnsServerRepository.getAllServers(),
                speedTestManager.currentTest,
                dnsSwitchManager.autoSwitchEnabled
            ) { servers, currentTest, autoSwitchEnabled ->
                val activeServer = servers.find { it.isActive }
                val fastestServers = servers.filter { it.latency > 0 }.sortedBy { it.latency }.take(3)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeServer = activeServer,
                    fastestServers = fastestServers,
                    currentSpeedTest = currentTest,
                    autoSwitchEnabled = autoSwitchEnabled,
                    isVpnConnected = _uiState.value.isVpnConnected
                )
            }
        }
    }
    
    fun refreshLatency() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                val servers = dnsServerRepository.getServersWithLatency()
                
                servers.forEach { server ->
                    val latency = dnsLatencyChecker.checkLatency(server.ip)
                    dnsServerRepository.updateServerLatency(server.id, latency)
                    latencyRepository.recordLatency(
                        serverId = server.id,
                        serverName = server.name,
                        serverIp = server.ip,
                        latency = latency,
                        success = latency > 0
                    )
                }
                
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRefreshing = false, error = e.message)
            }
        }
    }
    
    fun toggleVpn() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isVpnConnected) {
                // Stop VPN
                _uiState.value = currentState.copy(isVpnConnected = false)
            } else {
                // Start VPN
                _uiState.value = currentState.copy(isVpnConnected = true)
            }
        }
    }
    
    fun switchToServer(serverId: String) {
        viewModelScope.launch {
            try {
                dnsSwitchManager.manualSwitch(serverId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getNetworkMetrics(): NetworkMetrics {
        val currentTest = _uiState.value.currentSpeedTest
        return if (currentTest != null) {
            NetworkMetrics(
                downloadSpeed = currentTest.downloadSpeed,
                uploadSpeed = currentTest.uploadSpeed,
                ping = currentTest.ping,
                jitter = currentTest.jitter,
                packetLoss = currentTest.packetLoss
            )
        } else {
            NetworkMetrics()
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isVpnConnected: Boolean = false,
    val activeServer: DNSServer? = null,
    val fastestServers: List<DNSServer> = emptyList(),
    val currentSpeedTest: com.photondns.app.service.SpeedTestResult? = null,
    val autoSwitchEnabled: Boolean = false,
    val error: String? = null
)
