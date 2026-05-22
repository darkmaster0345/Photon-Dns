package com.photondns.app.presentation.ui.components

import com.photondns.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AnimationPreferences {
    fun animationsEnabled(settingsRepository: SettingsRepository): Flow<Boolean> {
        return settingsRepository.appSettingsFlow.map { it.animationsEnabled }
    }
}
