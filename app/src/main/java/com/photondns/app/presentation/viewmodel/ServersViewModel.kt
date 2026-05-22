package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.DNSProtocol
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.SettingsRepository
import com.photondns.app.service.DNSLatencyChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.net.InetAddress

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val dnsServerRepository: DNSServerRepository,
    private val dnsLatencyChecker: DNSLatencyChecker,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServersUiState())
    val uiState: StateFlow<ServersUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observeServers()
        observeSettings()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                dnsServerRepository.ensureDefaultServersInitialized()
                val servers = dnsServerRepository.getAllServersList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    servers = servers
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    private fun observeServers() {
        viewModelScope.launch {
            dnsServerRepository.getAllServers().collect { servers ->
                val currentQuery = _uiState.value.searchQuery
                val filtered = filterServers(servers, currentQuery)
                _uiState.value = _uiState.value.copy(
                    servers = servers,
                    filteredServers = filtered,
                    isLoading = false
                )
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.appSettingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(animationsEnabled = settings.animationsEnabled)
            }
        }
    }
    
    fun refreshLatency() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                val servers = _uiState.value.servers
                servers.forEach { server ->
                    val latency = dnsLatencyChecker.checkLatency(
                        serverIp = server.ip,
                        protocol = server.protocol,
                        dohUrl = server.dohUrl,
                        dotHostname = server.dotHostname
                    )
                    dnsServerRepository.updateServerLatency(server.id, latency)
                }
                
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRefreshing = false, error = e.message)
            }
        }
    }
    
    fun switchToServer(serverId: String) {
        viewModelScope.launch {
            try {
                dnsServerRepository.setActiveServer(serverId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addCustomServer(
        name: String,
        ip: String,
        countryCode: String,
        protocol: DNSProtocol,
        dohUrl: String? = null,
        dotHostname: String? = null
    ) {
        viewModelScope.launch {
            try {
                val normalizedIp = ip.trim()
                val normalizedDohUrl = dohUrl?.trim()?.takeIf { it.isNotEmpty() }
                val normalizedDotHostname = dotHostname?.trim()?.takeIf { it.isNotEmpty() }

                // Validate protocol-specific fields
                if (protocol == DNSProtocol.DOH && normalizedDohUrl == null) {
                    _uiState.value = _uiState.value.copy(error = "DoH URL is required for DoH protocol")
                    return@launch
                }
                if (protocol == DNSProtocol.DOT && normalizedDotHostname == null) {
                    _uiState.value = _uiState.value.copy(error = "DoT Hostname is required for DoT protocol")
                    return@launch
                }

                val server = DNSServer(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name.trim(),
                    ip = normalizedIp,
                    countryCode = countryCode.trim().uppercase(),
                    isCustom = true,
                    protocol = protocol,
                    dohUrl = normalizedDohUrl,
                    dotHostname = normalizedDotHostname
                )

                dnsServerRepository.insertServer(server)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            try {
                dnsServerRepository.deleteServerById(serverId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun searchServers(query: String) {
        val filteredServers = filterServers(_uiState.value.servers, query)

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredServers = filteredServers
        )
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filteredServers = _uiState.value.servers
        )
    }
    
    private fun filterServers(servers: List<DNSServer>, query: String): List<DNSServer> {
        if (query.isBlank()) return servers
        return servers.filter { server ->
            server.name.contains(query, ignoreCase = true) ||
                server.ip.contains(query, ignoreCase = true) ||
                server.countryCode.contains(query, ignoreCase = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ServersUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val servers: List<DNSServer> = emptyList(),
    val filteredServers: List<DNSServer> = emptyList(),
    val searchQuery: String = "",
    val animationsEnabled: Boolean = true,
    val error: String? = null
)
