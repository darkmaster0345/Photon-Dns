package com.photondns.app.di

import android.content.Context
import androidx.room.Room
import com.photondns.app.data.database.PhotonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun providePhotonDatabase(
        @ApplicationContext context: Context
    ): PhotonDatabase {
        return Room.databaseBuilder(
            context,
            PhotonDatabase::class.java,
            "photon_dns_database"
        ).fallbackToDestructiveMigration().build()
    }
    
    @Provides
    fun provideDnsServerDao(database: PhotonDatabase) = database.dnsServerDao()
    
    @Provides
    fun provideSpeedTestDao(database: PhotonDatabase) = database.speedTestDao()
    
    @Provides
    fun provideLatencyDao(database: PhotonDatabase) = database.latencyDao()
    
    @Provides
    fun provideSwitchEventDao(database: PhotonDatabase) = database.switchEventDao()
}
