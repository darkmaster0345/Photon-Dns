package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.service.DNSLatencyChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.InetAddress

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val dnsServerRepository: DNSServerRepository,
    private val dnsLatencyChecker: DNSLatencyChecker
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServersUiState())
    val uiState: StateFlow<ServersUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observeServers()
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
    
    fun refreshLatency() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                val servers = _uiState.value.servers
                servers.forEach { server ->
                    val latency = dnsLatencyChecker.checkLatency(server.ip)
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
    
    fun addCustomServer(name: String, ip: String, countryCode: String) {
        viewModelScope.launch {
            try {
                val normalizedIp = ip.trim()
                if (!isValidDnsEndpoint(normalizedIp)) {
                    _uiState.value = _uiState.value.copy(error = "Enter a valid DNS IP or DoH URL")
                    return@launch
                }

                val duplicate = _uiState.value.servers.any { it.ip.equals(normalizedIp, ignoreCase = true) }
                if (duplicate) {
                    _uiState.value = _uiState.value.copy(error = "This DNS server already exists")
                    return@launch
                }

                val server = DNSServer(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name.trim(),
                    ip = normalizedIp,
                    countryCode = countryCode.trim().uppercase(),
                    isCustom = true
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
    
    fun getFastestServers(): List<DNSServer> {
        return _uiState.value.servers
            .filter { it.latency > 0 }
            .sortedBy { it.latency }
            .take(3)
    }
    
    private fun filterServers(servers: List<DNSServer>, query: String): List<DNSServer> {
        if (query.isBlank()) return servers
        return servers.filter { server ->
            server.name.contains(query, ignoreCase = true) ||
                server.ip.contains(query, ignoreCase = true) ||
                server.countryCode.contains(query, ignoreCase = true)
        }
    }

    private fun isValidDnsEndpoint(value: String): Boolean {
        return value.startsWith("https://") || runCatching { InetAddress.getByName(value); true }.getOrDefault(false)
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
    val error: String? = null
)
