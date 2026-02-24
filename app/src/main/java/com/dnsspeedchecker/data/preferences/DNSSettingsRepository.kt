package com.dnsspeedchecker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing DNS settings using DataStore
 */
class DNSSettingsRepository(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dns_settings")
    
    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }
        
        // Preference keys
        private val AUTO_SWITCH_ENABLED_KEY = booleanPreferencesKey("auto_switch_enabled")
        private val STRATEGY_KEY = stringPreferencesKey("strategy")
        private val CUSTOM_CHECK_INTERVAL_KEY = intPreferencesKey("custom_check_interval")
        private val CUSTOM_MIN_IMPROVEMENT_KEY = intPreferencesKey("custom_min_improvement")
        private val CUSTOM_CONSECUTIVE_CHECKS_KEY = intPreferencesKey("custom_consecutive_checks")
        private val CUSTOM_STABILITY_PERIOD_KEY = intPreferencesKey("custom_stability_period")
        private val HYSTERESIS_ENABLED_KEY = booleanPreferencesKey("hysteresis_enabled")
        private val HYSTERESIS_MARGIN_KEY = intPreferencesKey("hysteresis_margin")
        private val SWITCH_ON_FAILURE_KEY = booleanPreferencesKey("switch_on_failure")
        private val BATTERY_SAVER_MODE_KEY = booleanPreferencesKey("battery_saver_mode")
        private val LAST_UPDATED_KEY = stringPreferencesKey("last_updated")
        
        // Default settings
        private val DEFAULT_SETTINGS = DNSSettings()
    }
    
    /**
     * Gets the DNS settings flow
     */
    val dnsSettingsFlow: Flow<DNSSettings> = context.dataStore.data.map { preferences ->
        DNSSettings(
            autoSwitchEnabled = preferences[AUTO_SWITCH_ENABLED_KEY] ?: DEFAULT_SETTINGS.autoSwitchEnabled,
            strategy = Strategy.valueOf(
                preferences[STRATEGY_KEY] ?: DEFAULT_SETTINGS.strategy.name
            ),
            customCheckInterval = preferences[CUSTOM_CHECK_INTERVAL_KEY] ?: DEFAULT_SETTINGS.customCheckInterval,
            customMinImprovement = preferences[CUSTOM_MIN_IMPROVEMENT_KEY] ?: DEFAULT_SETTINGS.customMinImprovement,
            customConsecutiveChecks = preferences[CUSTOM_CONSECUTIVE_CHECKS_KEY] ?: DEFAULT_SETTINGS.customConsecutiveChecks,
            customStabilityPeriod = preferences[CUSTOM_STABILITY_PERIOD_KEY] ?: DEFAULT_SETTINGS.customStabilityPeriod,
            hysteresisEnabled = preferences[HYSTERESIS_ENABLED_KEY] ?: DEFAULT_SETTINGS.hysteresisEnabled,
            hysteresisMargin = preferences[HYSTERESIS_MARGIN_KEY] ?: DEFAULT_SETTINGS.hysteresisMargin,
            switchOnFailure = preferences[SWITCH_ON_FAILURE_KEY] ?: DEFAULT_SETTINGS.switchOnFailure,
            batterySaverMode = preferences[BATTERY_SAVER_MODE_KEY] ?: DEFAULT_SETTINGS.batterySaverMode,
            lastUpdated = preferences[LAST_UPDATED_KEY]?.toLongOrNull() ?: DEFAULT_SETTINGS.lastUpdated
        )
    }
    
    /**
     * Updates auto-switch enabled setting
     */
    suspend fun setAutoSwitchEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SWITCH_ENABLED_KEY] = enabled
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates the switching strategy
     */
    suspend fun setStrategy(strategy: Strategy) {
        context.dataStore.edit { preferences ->
            preferences[STRATEGY_KEY] = strategy.name
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates custom check interval
     */
    suspend fun setCustomCheckInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_CHECK_INTERVAL_KEY] = interval.coerceIn(5, 60)
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates custom minimum improvement
     */
    suspend fun setCustomMinImprovement(improvement: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_MIN_IMPROVEMENT_KEY] = improvement.coerceIn(10, 100)
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates custom consecutive checks
     */
    suspend fun setCustomConsecutiveChecks(checks: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_CONSECUTIVE_CHECKS_KEY] = checks.coerceIn(2, 10)
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates custom stability period
     */
    suspend fun setCustomStabilityPeriod(period: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_STABILITY_PERIOD_KEY] = period.coerceIn(1, 10)
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates hysteresis enabled setting
     */
    suspend fun setHysteresisEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HYSTERESIS_ENABLED_KEY] = enabled
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates hysteresis margin
     */
    suspend fun setHysteresisMargin(margin: Int) {
        context.dataStore.edit { preferences ->
            preferences[HYSTERESIS_MARGIN_KEY] = margin.coerceIn(5, 50)
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates switch on failure setting
     */
    suspend fun setSwitchOnFailure(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SWITCH_ON_FAILURE_KEY] = enabled
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates battery saver mode setting
     */
    suspend fun setBatterySaverMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BATTERY_SAVER_MODE_KEY] = enabled
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Updates all DNS settings at once
     */
    suspend fun updateDNSSettings(settings: DNSSettings) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SWITCH_ENABLED_KEY] = settings.autoSwitchEnabled
            preferences[STRATEGY_KEY] = settings.strategy.name
            preferences[CUSTOM_CHECK_INTERVAL_KEY] = settings.customCheckInterval.coerceIn(5, 60)
            preferences[CUSTOM_MIN_IMPROVEMENT_KEY] = settings.customMinImprovement.coerceIn(10, 100)
            preferences[CUSTOM_CONSECUTIVE_CHECKS_KEY] = settings.customConsecutiveChecks.coerceIn(2, 10)
            preferences[CUSTOM_STABILITY_PERIOD_KEY] = settings.customStabilityPeriod.coerceIn(1, 10)
            preferences[HYSTERESIS_ENABLED_KEY] = settings.hysteresisEnabled
            preferences[HYSTERESIS_MARGIN_KEY] = settings.hysteresisMargin.coerceIn(5, 50)
            preferences[SWITCH_ON_FAILURE_KEY] = settings.switchOnFailure
            preferences[BATTERY_SAVER_MODE_KEY] = settings.batterySaverMode
            preferences[LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Exports settings to JSON string
     */
    suspend fun exportSettings(): String {
        val settings = dnsSettingsFlow.let { flow ->
            // Collect the current settings
            kotlinx.coroutines.runBlocking {
                flow.collect { it }
            }
        }
        return JSON.encodeToString(settings)
    }
    
    /**
     * Imports settings from JSON string
     */
    suspend fun importSettings(jsonString: String): Result<DNSSettings> {
        return try {
            val settings = JSON.decodeFromString<DNSSettings>(jsonString)
            val validationIssues = settings.validate()
            
            if (validationIssues.isEmpty()) {
                updateDNSSettings(settings)
                Result.success(settings)
            } else {
                Result.failure(Exception("Invalid settings: ${validationIssues.joinToString(", ")}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resets all settings to defaults
     */
    suspend fun resetToDefaults() {
        updateDNSSettings(DEFAULT_SETTINGS)
    }
    
    /**
     * Gets current settings synchronously (for immediate access)
     */
    suspend fun getCurrentSettings(): DNSSettings {
        return kotlinx.coroutines.runBlocking {
            dnsSettingsFlow.let { flow ->
                var result = DEFAULT_SETTINGS
                flow.collect { result = it }
                result
            }
        }
    }
}
