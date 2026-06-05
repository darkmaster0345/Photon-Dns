package com.photondns.app.receiver

import com.photondns.app.data.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsRepositoryEntryPoint {
    fun settingsRepository(): SettingsRepository
}