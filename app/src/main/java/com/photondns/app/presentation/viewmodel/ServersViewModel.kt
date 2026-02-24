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
                _uiState.value = _uiState.value.copy(
                    servers = servers,
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
                val server = DNSServer(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name,
                    ip = ip,
                    countryCode = countryCode,
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
        val filteredServers = if (query.isBlank()) {
            _uiState.value.servers
        } else {
            _uiState.value.servers.filter { server ->
                server.name.contains(query, ignoreCase = true) ||
                server.ip.contains(query, ignoreCase = true) ||
                server.countryCode.contains(query, ignoreCase = true)
            }
        }
        
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
