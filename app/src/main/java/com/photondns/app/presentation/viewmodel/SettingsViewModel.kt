package com.photondns.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photondns.app.data.models.AppSettings
import com.photondns.app.data.models.SwitchStrategy
import com.photondns.app.service.DNSSwitchManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dnsSwitchManager: DNSSwitchManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        observeSettings()
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
            dnsSwitchManager.autoSwitchEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(autoSwitchEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            dnsSwitchManager.currentStrategy.collect { strategy ->
                _uiState.value = _uiState.value.copy(currentStrategy = strategy)
            }
        }
    }
    
    fun toggleAutoSwitch() {
        val currentState = _uiState.value.autoSwitchEnabled
        dnsSwitchManager.setAutoSwitchEnabled(!currentState)
    }
    
    fun updateStrategy(strategy: SwitchStrategy) {
        dnsSwitchManager.updateStrategy(strategy)
    }
    
    fun selectPresetStrategy(presetName: String) {
        val preset = SwitchStrategy.getPresets().find { it.name == presetName }
        if (preset != null) {
            updateStrategy(preset)
        }
    }
    
    fun updateCustomStrategy(
        checkInterval: Int,
        minImprovement: Int,
        consecutiveChecks: Int,
        stabilityPeriod: Int
    ) {
        val customStrategy = SwitchStrategy(
            name = "Custom",
            checkInterval = checkInterval,
            minImprovement = minImprovement,
            consecutiveChecks = consecutiveChecks,
            stabilityPeriod = stabilityPeriod
        )
        updateStrategy(customStrategy)
    }
    
    fun updateAppSettings(settings: AppSettings) {
        _uiState.value = _uiState.value.copy(appSettings = settings)
        // Save to DataStore would be implemented here
    }
    
    fun getAvailableStrategies(): List<SwitchStrategy> {
        return SwitchStrategy.getPresets()
    }
    
    fun resetToDefaults() {
        val defaultStrategy = SwitchStrategy.getPresets()[1] // Balanced
        updateStrategy(defaultStrategy)
        
        val defaultSettings = AppSettings()
        updateAppSettings(defaultSettings)
    }
    
    fun exportSettings(): String {
        val settings = _uiState.value
        return buildString {
            appendLine("Photon DNS Settings Export")
            appendLine("==========================")
            appendLine("Auto Switch Enabled: ${settings.autoSwitchEnabled}")
            appendLine("Strategy: ${settings.currentStrategy.name}")
            appendLine("Check Interval: ${settings.currentStrategy.checkInterval}s")
            appendLine("Min Improvement: ${settings.currentStrategy.minImprovement}ms")
            appendLine("Consecutive Checks: ${settings.currentStrategy.consecutiveChecks}")
            appendLine("Stability Period: ${settings.currentStrategy.stabilityPeriod}min")
            appendLine("Hysteresis Enabled: ${settings.appSettings.hysteresisEnabled}")
            appendLine("Battery Saver: ${settings.appSettings.batterySaverMode}")
            appendLine("Switch on Failure: ${settings.appSettings.switchOnFailure}")
            appendLine("Notifications: ${settings.appSettings.notificationsEnabled}")
        }
    }
}

data class SettingsUiState(
    val autoSwitchEnabled: Boolean = false,
    val currentStrategy: SwitchStrategy = SwitchStrategy.getPresets()[1],
    val appSettings: AppSettings = AppSettings(),
    val isExporting: Boolean = false,
    val error: String? = null
)
