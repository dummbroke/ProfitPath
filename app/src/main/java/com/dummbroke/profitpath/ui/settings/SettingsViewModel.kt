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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsRepository: SettingsRepository = SettingsRepository(application.applicationContext)

    // User Profile Data
    private val userProfileFlow: StateFlow<UserProfile?> = settingsRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Start with null, will be updated from Firestore
        )

    val userEmail: StateFlow<String> = userProfileFlow
        .map { it?.email ?: "Loading..." }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading...")

    val traderName: StateFlow<String> = userProfileFlow
        .map { it?.displayName ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val tradingStyle: StateFlow<String> = userProfileFlow
        .map { it?.tradingStyle ?: "day_trader" } // Default if not set
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "day_trader")

    val currentBalance: StateFlow<String> = userProfileFlow
        .map { profile ->
            val balance = profile?.currentBalance ?: 0.0
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            if (balance == 0.0) "0.00" else formatter.format(balance).replace("$","") // Format as string, remove $ if present by default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.00")

    fun updateTraderName(name: String) {
        viewModelScope.launch {
            settingsRepository.updateDisplayName(name)
            // Optionally, handle Result<Unit> for success/failure feedback
        }
    }

    fun updateTradingStyle(styleId: String) {
        viewModelScope.launch {
            settingsRepository.updateTradingStyle(styleId)
            // Optionally, handle Result<Unit>
        }
    }

    fun updateCurrentBalance(balance: Double) {
        viewModelScope.launch {
            settingsRepository.updateCurrentBalance(balance)
            // Optionally, handle Result<Unit>
        }
    }

    // --- Existing Theme Toggle Logic ---
    val isDarkMode: StateFlow<Boolean> = settingsRepository.getThemePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(isDark)
        }
    }

    // --- Existing Logout Logic ---
    private val _showLogoutConfirmDialog = MutableStateFlow(false)
    val showLogoutConfirmDialog: StateFlow<Boolean> = _showLogoutConfirmDialog.asStateFlow()

    private val _logoutConfirmedEvent = MutableSharedFlow<Unit>()
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
            // Actual logout logic (e.g., FirebaseAuth.getInstance().signOut()) should be handled here or in repo
        }
        _showLogoutConfirmDialog.value = false
    }

    // --- Placeholder for other settings data ---
    val cloudSyncStatus: StateFlow<String> = MutableStateFlow("Last synced: Not implemented").asStateFlow()
    val screenshotCacheSize: StateFlow<String> = MutableStateFlow("Calculating...").asStateFlow()

    // --- Placeholder for account deletion and cache clearing ---
    // These would typically involve more complex logic, confirmation, and repository calls
    private val _showDeleteAccountConfirmDialog = MutableStateFlow(false)
    val showDeleteAccountConfirmDialog: StateFlow<Boolean> = _showDeleteAccountConfirmDialog.asStateFlow()

    fun deleteAccount() {
        // TODO: Implement actual account deletion logic in repository
        // e.g., firebaseAuth.currentUser?.delete() + delete user data from Firestore
        viewModelScope.launch {
            // settingsRepository.deleteCurrentUserAccount()
             _logoutConfirmedEvent.emit(Unit) // Trigger logout flow after deletion
        }
        _showDeleteAccountConfirmDialog.value = false
    }

    fun clearScreenshotCache() {
        // TODO: Implement actual cache clearing logic (local file deletion)
        viewModelScope.launch {
            // settingsRepository.clearLocalImageCache()
            // Update screenshotCacheSize accordingly
        }
    }
} 