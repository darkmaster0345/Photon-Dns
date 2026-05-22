package com.photondns.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.photondns.app.data.models.AppSettings
import com.photondns.app.data.models.SwitchStrategy
import com.photondns.app.data.models.VpnMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val AUTO_SWITCH_ENABLED = booleanPreferencesKey("auto_switch_enabled")
        val CURRENT_STRATEGY = stringPreferencesKey("current_strategy")
        val HYSTERESIS_ENABLED = booleanPreferencesKey("hysteresis_enabled")
        val BATTERY_SAVER_MODE = booleanPreferencesKey("battery_saver_mode")
        val SWITCH_ON_FAILURE = booleanPreferencesKey("switch_on_failure")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val SPEED_TEST_SERVER = stringPreferencesKey("speed_test_server")
        val VPN_MODE = stringPreferencesKey("vpn_mode")
        val IPV6_ENABLED = booleanPreferencesKey("ipv6_enabled")

        // Custom strategy properties
        val CUSTOM_STRATEGY_CHECK_INTERVAL = intPreferencesKey("custom_strategy_check_interval")
        val CUSTOM_STRATEGY_MIN_IMPROVEMENT = intPreferencesKey("custom_strategy_min_improvement")
        val CUSTOM_STRATEGY_CONSECUTIVE_CHECKS = intPreferencesKey("custom_strategy_consecutive_checks")
        val CUSTOM_STRATEGY_STABILITY_PERIOD = intPreferencesKey("custom_strategy_stability_period")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val defaultSettings = AppSettings()
            AppSettings(
                autoSwitchEnabled = preferences[PreferencesKeys.AUTO_SWITCH_ENABLED] ?: defaultSettings.autoSwitchEnabled,
                currentStrategy = preferences[PreferencesKeys.CURRENT_STRATEGY] ?: defaultSettings.currentStrategy,
                customStrategy = SwitchStrategy(
                    name = "Custom",
                    checkInterval = preferences[PreferencesKeys.CUSTOM_STRATEGY_CHECK_INTERVAL] ?: defaultSettings.customStrategy.checkInterval,
                    minImprovement = preferences[PreferencesKeys.CUSTOM_STRATEGY_MIN_IMPROVEMENT] ?: defaultSettings.customStrategy.minImprovement,
                    consecutiveChecks = preferences[PreferencesKeys.CUSTOM_STRATEGY_CONSECUTIVE_CHECKS] ?: defaultSettings.customStrategy.consecutiveChecks,
                    stabilityPeriod = preferences[PreferencesKeys.CUSTOM_STRATEGY_STABILITY_PERIOD] ?: defaultSettings.customStrategy.stabilityPeriod
                ),
                hysteresisEnabled = preferences[PreferencesKeys.HYSTERESIS_ENABLED] ?: defaultSettings.hysteresisEnabled,
                batterySaverMode = preferences[PreferencesKeys.BATTERY_SAVER_MODE] ?: defaultSettings.batterySaverMode,
                switchOnFailure = preferences[PreferencesKeys.SWITCH_ON_FAILURE] ?: defaultSettings.switchOnFailure,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: defaultSettings.notificationsEnabled,
                darkMode = preferences[PreferencesKeys.DARK_MODE] ?: defaultSettings.darkMode,
                animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: defaultSettings.animationsEnabled,
                speedTestServer = preferences[PreferencesKeys.SPEED_TEST_SERVER] ?: defaultSettings.speedTestServer,
                vpnMode = try {
                    VpnMode.valueOf(preferences[PreferencesKeys.VPN_MODE] ?: defaultSettings.vpnMode.name)
                } catch (e: Exception) {
                    defaultSettings.vpnMode
                },
                ipv6Enabled = preferences[PreferencesKeys.IPV6_ENABLED] ?: defaultSettings.ipv6Enabled
            )
        }

    suspend fun updateAppSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SWITCH_ENABLED] = settings.autoSwitchEnabled
            preferences[PreferencesKeys.CURRENT_STRATEGY] = settings.currentStrategy
            preferences[PreferencesKeys.HYSTERESIS_ENABLED] = settings.hysteresisEnabled
            preferences[PreferencesKeys.BATTERY_SAVER_MODE] = settings.batterySaverMode
            preferences[PreferencesKeys.SWITCH_ON_FAILURE] = settings.switchOnFailure
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[PreferencesKeys.DARK_MODE] = settings.darkMode
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = settings.animationsEnabled
            preferences[PreferencesKeys.SPEED_TEST_SERVER] = settings.speedTestServer
            preferences[PreferencesKeys.VPN_MODE] = settings.vpnMode.name
            preferences[PreferencesKeys.IPV6_ENABLED] = settings.ipv6Enabled

            // Custom strategy
            preferences[PreferencesKeys.CUSTOM_STRATEGY_CHECK_INTERVAL] = settings.customStrategy.checkInterval
            preferences[PreferencesKeys.CUSTOM_STRATEGY_MIN_IMPROVEMENT] = settings.customStrategy.minImprovement
            preferences[PreferencesKeys.CUSTOM_STRATEGY_CONSECUTIVE_CHECKS] = settings.customStrategy.consecutiveChecks
            preferences[PreferencesKeys.CUSTOM_STRATEGY_STABILITY_PERIOD] = settings.customStrategy.stabilityPeriod
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }
}
