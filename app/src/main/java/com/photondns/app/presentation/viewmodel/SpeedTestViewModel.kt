package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.SpeedTestResult
import com.photondns.app.data.repository.SettingsRepository
import com.photondns.app.data.repository.SpeedTestRepository
import com.photondns.app.service.SpeedTestManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val speedTestManager: SpeedTestManager,
    private val speedTestRepository: SpeedTestRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    val testProgress = speedTestManager.testProgress
    
    init {
        observeSpeedTest()
        loadHistory()
    }
    
    private fun observeSpeedTest() {
        viewModelScope.launch {
            speedTestManager.currentTest.collect { result ->
                _uiState.value = _uiState.value.copy(currentResult = result)
            }
        }

        viewModelScope.launch {
            settingsRepository.appSettingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(animationsEnabled = settings.animationsEnabled)
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
                    error = null,
                    currentResult = null
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
                    loadHistory()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Speed test failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isTestRunning = false)
            }
        }
    }
    
    fun cancelTest() {
        viewModelScope.launch {
            speedTestManager.cancelTest()
            _uiState.value = _uiState.value.copy(isTestRunning = false, currentResult = null)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SpeedTestUiState(
    val isLoading: Boolean = true,
    val isTestRunning: Boolean = false,
    val currentResult: com.photondns.app.service.SpeedTestResult? = null,
    val testHistory: List<SpeedTestResult> = emptyList(),
    val animationsEnabled: Boolean = true,
    val error: String? = null
)
