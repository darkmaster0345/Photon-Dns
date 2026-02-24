package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.LatencyRecord
import com.photondns.app.data.models.DNSSwitchEvent
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.LatencyRepository
import com.photondns.app.service.DNSLatencyChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val dnsServerRepository: DNSServerRepository,
    private val latencyRepository: LatencyRepository,
    private val dnsLatencyChecker: DNSLatencyChecker
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observeData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                dnsServerRepository.ensureDefaultServersInitialized()
                val now = System.currentTimeMillis()
                val oneHourAgo = now - (60 * 60 * 1000)
                
                val latencyRecords = latencyRepository.getLatencyRecordsSince(oneHourAgo)
                val switchEvents = latencyRepository.getSwitchEventsSince(oneHourAgo)
                val servers = dnsServerRepository.getAllServersList()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    latencyRecords = latencyRecords,
                    switchEvents = switchEvents,
                    servers = servers
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    private fun observeData() {
        viewModelScope.launch {
            latencyRepository.getAllLatencyRecords().collect { records ->
                _uiState.value = _uiState.value.copy(latencyRecords = records)
            }
        }
        
        viewModelScope.launch {
            latencyRepository.getAllSwitchEvents().collect { events ->
                _uiState.value = _uiState.value.copy(switchEvents = events)
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                // Update latency for all servers
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
    
    fun getLatencyDataForGraph(): List<LatencyDataPoint> {
        val records = _uiState.value.latencyRecords
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - (60 * 1000)
        
        // Get records from the last minute for real-time graph
        val recentRecords = records.filter { it.timestamp >= oneMinuteAgo }
        
        return recentRecords.groupBy { 
            // Group by 5-second intervals
            (it.timestamp / 5000) * 5000 
        }.map { (timestamp, recordGroup) ->
            LatencyDataPoint(
                timestamp = timestamp,
                latencyValues = recordGroup.groupBy { it.dnsServerId }.mapValues { entry ->
                    entry.value.filter { it.success }.map { it.latency }.average()
                }
            )
        }.sortedBy { it.timestamp }
    }
    
    fun getServerPerformanceStats(): Map<String, ServerPerformanceStats> {
        val records = _uiState.value.latencyRecords
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        
        val recentRecords = records.filter { it.timestamp >= oneHourAgo }
        
        return recentRecords.groupBy { it.dnsServerId }.mapValues { (serverId, serverRecords) ->
            val successfulRecords = serverRecords.filter { it.success }
            val avgLatency = if (successfulRecords.isNotEmpty()) {
                successfulRecords.map { it.latency }.average()
            } else 0.0
            
            val uptime = if (serverRecords.isNotEmpty()) {
                (successfulRecords.size.toDouble() / serverRecords.size) * 100
            } else 0.0
            
            ServerPerformanceStats(
                serverId = serverId,
                serverName = serverRecords.firstOrNull()?.dnsServerName ?: "",
                averageLatency = avgLatency,
                uptime = uptime,
                totalChecks = serverRecords.size,
                successfulChecks = successfulRecords.size
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MonitorUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val latencyRecords: List<LatencyRecord> = emptyList(),
    val switchEvents: List<DNSSwitchEvent> = emptyList(),
    val servers: List<com.photondns.app.data.models.DNSServer> = emptyList(),
    val error: String? = null
)

data class LatencyDataPoint(
    val timestamp: Long,
    val latencyValues: Map<String, Double> // Server ID to average latency
)

data class ServerPerformanceStats(
    val serverId: String,
    val serverName: String,
    val averageLatency: Double,
    val uptime: Double,
    val totalChecks: Int,
    val successfulChecks: Int
)
