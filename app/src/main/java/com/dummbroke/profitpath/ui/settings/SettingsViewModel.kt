package com.dummbroke.profitpath.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _showLogoutConfirmDialog = MutableStateFlow(false)
    val showLogoutConfirmDialog: StateFlow<Boolean> = _showLogoutConfirmDialog.asStateFlow()

    private val _logoutConfirmedEvent = MutableSharedFlow<Unit>() // Used as a one-time event
    val logoutConfirmedEvent = _logoutConfirmedEvent.asSharedFlow()

    fun onLogoutClicked() {
        _showLogoutConfirmDialog.value = true
    }

    fun onDismissLogoutDialog() {
        _showLogoutConfirmDialog.value = false
    }

    fun onLogoutConfirmed() {
        viewModelScope.launch {
            _logoutConfirmedEvent.emit(Unit)
        }
        _showLogoutConfirmDialog.value = false // Dismiss dialog after confirmation
    }
} 