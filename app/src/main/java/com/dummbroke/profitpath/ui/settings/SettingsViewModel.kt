package com.dummbroke.profitpath.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsRepository: SettingsRepository = SettingsRepository(application.applicationContext)

    val isDarkMode: StateFlow<Boolean> = settingsRepository.getThemePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Default to dark mode, will be quickly updated by DataStore
        )

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(isDark)
        }
    }
} 