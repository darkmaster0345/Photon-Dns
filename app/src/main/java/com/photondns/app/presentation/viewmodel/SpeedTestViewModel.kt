package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.SpeedTestResult
import com.photondns.app.data.repository.SpeedTestRepository
import com.photondns.app.service.SpeedTestManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val speedTestManager: SpeedTestManager,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()
    
    init {
        observeSpeedTest()
        loadHistory()
    }
    
    private fun observeSpeedTest() {
        viewModelScope.launch {
            speedTestManager.testProgress.collect { progress ->
                _uiState.value = _uiState.value.copy(testProgress = progress)
            }
        }
        
        viewModelScope.launch {
            speedTestManager.currentTest.collect { result ->
                _uiState.value = _uiState.value.copy(
                    currentTest = result,
                    isTestRunning = result != null && speedTestManager.isTestRunning()
                )
            }
        }
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = speedTestRepository.getRecentSpeedTests(20)
                _uiState.value = _uiState.value.copy(
                    testHistory = history,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun startSpeedTest(testServer: String = "auto") {
        if (_uiState.value.isTestRunning) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isTestRunning = true,
                    error = null
                )
                
                val result = speedTestManager.runSpeedTest(testServer)
                
                if (result != null) {
                    speedTestRepository.insertSpeedTest(
                        SpeedTestResult(
                            timestamp = result.timestamp,
                            downloadSpeed = result.downloadSpeed,
                            uploadSpeed = result.uploadSpeed,
                            ping = result.ping,
                            jitter = result.jitter,
                            packetLoss = result.packetLoss,
                            testServer = result.testServer,
                            dnsUsed = result.dnsUsed,
                            testDuration = result.testDuration
                        )
                    )
                    
                    // Refresh history
                    loadHistory()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Speed test failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestRunning = false,
                    error = e.message
                )
            }
        }
    }
    
    fun cancelSpeedTest() {
        viewModelScope.launch {
            speedTestManager.cancelTest()
            _uiState.value = _uiState.value.copy(isTestRunning = false)
        }
    }
    
    fun shareResults(result: SpeedTestResult) {
        // This would implement sharing functionality
        // For now, just return the formatted string
        val shareText = buildString {
            appendLine("Speed Test Results")
            appendLine("===================")
            appendLine("Download: ${String.format("%.1f", result.downloadSpeed)} Mbps")
            appendLine("Upload: ${String.format("%.1f", result.uploadSpeed)} Mbps")
            appendLine("Ping: ${result.ping} ms")
            appendLine("Jitter: ${result.jitter} ms")
            appendLine("Packet Loss: ${String.format("%.1f", result.packetLoss)}%")
            appendLine("Server: ${result.testServer}")
            appendLine("DNS: ${result.dnsUsed}")
            appendLine("Duration: ${result.testDuration} ms")
            appendLine("===================")
            appendLine("Tested with Photon DNS - DNS at the speed of light!")
        }

        _uiState.value = _uiState.value.copy(shareText = shareText)
    }

    fun clearShareText() {
        _uiState.value = _uiState.value.copy(shareText = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getSpeedTestStats(): SpeedTestStats? {
        val history = _uiState.value.testHistory
        if (history.isEmpty()) return null
        
        val avgDownload = history.map { it.downloadSpeed }.average()
        val avgUpload = history.map { it.uploadSpeed }.average()
        val avgPing = history.map { it.ping }.average()
        
        return SpeedTestStats(
            averageDownloadSpeed = avgDownload,
            averageUploadSpeed = avgUpload,
            averagePing = avgPing,
            testCount = history.size
        )
    }
}

data class SpeedTestUiState(
    val isLoading: Boolean = true,
    val isTestRunning: Boolean = false,
    val testProgress: Float = 0f,
    val currentTest: com.photondns.app.service.SpeedTestResult? = null,
    val testHistory: List<SpeedTestResult> = emptyList(),
    val error: String? = null,
    val shareText: String? = null
)

data class SpeedTestStats(
    val averageDownloadSpeed: Double,
    val averageUploadSpeed: Double,
    val averagePing: Double,
    val testCount: Int
)

