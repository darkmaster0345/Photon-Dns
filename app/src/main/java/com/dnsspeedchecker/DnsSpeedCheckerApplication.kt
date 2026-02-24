package com.dnsspeedchecker

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.dnsspeedchecker.data.database.DnsDatabase
import com.dnsspeedchecker.data.preferences.DNSSettingsRepository
import com.dnsspeedchecker.data.repository.DnsRepository
import com.dnsspeedchecker.data.repository.DnsRepositoryImpl
import com.dnsspeedchecker.data.repository.SettingsRepository
import com.dnsspeedchecker.data.repository.SettingsRepositoryImpl
import com.dnsspeedchecker.service.DnsVpnService

class DnsSpeedCheckerApplication : Application(), Configuration.Provider {
    
    // Database
    val database by lazy { DnsDatabase.getDatabase(this) }
    
    // Repositories
    val dnsRepository: DnsRepository by lazy { 
        DnsRepositoryImpl(database.dnsResultDao()) 
    }
    
    val settingsRepository: SettingsRepository by lazy { 
        SettingsRepositoryImpl(this) 
    }
    
    val dnsSettingsRepository: DNSSettingsRepository by lazy {
        DNSSettingsRepository(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}
