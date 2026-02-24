package com.dnsspeedchecker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dns_settings")

interface SettingsRepository {
    suspend fun setCheckInterval(interval: Long)
    fun getCheckInterval(): Flow<Long>
    
    suspend fun setSwitchingThreshold(threshold: Long)
    fun getSwitchingThreshold(): Flow<Long>
    
    suspend fun setAutoSwitchEnabled(enabled: Boolean)
    fun getAutoSwitchEnabled(): Flow<Boolean>
    
    suspend fun setEnabledDnsServers(servers: Set<String>)
    fun getEnabledDnsServers(): Flow<Set<String>>
    
    suspend fun setCurrentDnsServer(serverId: String)
    fun getCurrentDnsServer(): Flow<String>
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    
    private object PreferencesKeys {
        val CHECK_INTERVAL = longPreferencesKey("check_interval")
        val SWITCHING_THRESHOLD = longPreferencesKey("switching_threshold")
        val AUTO_SWITCH_ENABLED = booleanPreferencesKey("auto_switch_enabled")
        val ENABLED_DNS_SERVERS = stringSetPreferencesKey("enabled_dns_servers")
        val CURRENT_DNS_SERVER = stringPreferencesKey("current_dns_server")
    }
    
    override suspend fun setCheckInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_INTERVAL] = interval
        }
    }
    
    override fun getCheckInterval(): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CHECK_INTERVAL] ?: 5000L // Default: 5 seconds
        }
    }
    
    override suspend fun setSwitchingThreshold(threshold: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWITCHING_THRESHOLD] = threshold
        }
    }
    
    override fun getSwitchingThreshold(): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SWITCHING_THRESHOLD] ?: 20L // Default: 20ms
        }
    }
    
    override suspend fun setAutoSwitchEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SWITCH_ENABLED] = enabled
        }
    }
    
    override fun getAutoSwitchEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTO_SWITCH_ENABLED] ?: false // Default: disabled
        }
    }
    
    override suspend fun setEnabledDnsServers(servers: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLED_DNS_SERVERS] = servers
        }
    }
    
    override fun getEnabledDnsServers(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ENABLED_DNS_SERVERS] ?: setOf("google", "cloudflare", "quad9", "opendns")
        }
    }
    
    override suspend fun setCurrentDnsServer(serverId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_DNS_SERVER] = serverId
        }
    }
    
    override fun getCurrentDnsServer(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CURRENT_DNS_SERVER] ?: "google" // Default: Google DNS
        }
    }
}
