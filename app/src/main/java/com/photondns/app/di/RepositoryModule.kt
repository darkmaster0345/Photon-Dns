package com.photondns.app.di

import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.LatencyRepository
import com.photondns.app.data.repository.SettingsRepository
import com.photondns.app.data.repository.SpeedTestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Other repositories are already annotated with @Singleton and @Inject
    // but if we want to be explicit or if they had interfaces, we'd put them here.
}
