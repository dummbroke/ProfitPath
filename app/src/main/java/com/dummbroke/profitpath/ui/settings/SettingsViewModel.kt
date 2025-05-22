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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsRepository: SettingsRepository = SettingsRepository(application.applicationContext)

    // --- Fix: Add backing property for currentBalance ---
    private val _currentBalance = MutableStateFlow("0.00")
    val currentBalance: StateFlow<String> = _currentBalance.asStateFlow()

    // User Profile Data
    private val userProfileFlow: StateFlow<UserProfile?> = settingsRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Start with null, will be updated from Firestore
        )

    init {
        // Update _currentBalance when userProfileFlow changes
        viewModelScope.launch {
            userProfileFlow.collect { profile ->
                val balance = profile?.currentBalance ?: 0.0
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.minimumFractionDigits = 2
                formatter.maximumFractionDigits = 2
                _currentBalance.value = if (balance == 0.0) "0.00" else formatter.format(balance).replace("$","")
            }
        }
    }

    val userEmail: StateFlow<String> = userProfileFlow
        .map { it?.email ?: "Loading..." }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading...")

    val traderName: StateFlow<String> = userProfileFlow
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val tradingStyle: StateFlow<String> = userProfileFlow
        .map { it?.tradingStyle ?: "day_trader" } // Default if not set
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "day_trader")

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

    fun updateCurrentBalance(newBalance: Double, resetAnchor: Boolean = false) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val profileRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
        profileRef.get().addOnSuccessListener { doc ->
            // Remove anchorBalance logic
            profileRef.update("currentBalance", newBalance)
            _currentBalance.value = newBalance.toString()
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
    val cloudSyncStatus: StateFlow<String> = settingsRepository.getCloudSyncStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Not synced")

    private val _operationFeedback = MutableStateFlow<String?>(null)
    val operationFeedback: StateFlow<String?> = _operationFeedback.asStateFlow()

    // --- Placeholder for account deletion and cache clearing ---
    // These would typically involve more complex logic, confirmation, and repository calls
    private val _showDeleteAccountConfirmDialog = MutableStateFlow(false)
    val showDeleteAccountConfirmDialog: StateFlow<Boolean> = _showDeleteAccountConfirmDialog.asStateFlow()

    fun deleteAccount() {
        viewModelScope.launch {
            val result = settingsRepository.deleteCurrentUserAccount()
            if (result.isSuccess) {
                _operationFeedback.value = "Account deleted successfully."
                _logoutConfirmedEvent.emit(Unit) // Trigger logout flow after deletion
            } else {
                _operationFeedback.value = "Failed to delete account: ${result.exceptionOrNull()?.localizedMessage}" 
            }
        }
        _showDeleteAccountConfirmDialog.value = false
    }

    fun clearScreenshotCache() {
        viewModelScope.launch {
            val result = settingsRepository.clearLocalImageCache()
            if (result.isSuccess) {
                _operationFeedback.value = "Screenshot cache cleared."
            } else {
                _operationFeedback.value = "Failed to clear cache: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun clearOperationFeedback() {
        _operationFeedback.value = null
    }

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _changePasswordResult = MutableStateFlow<String?>(null)
    val changePasswordResult: StateFlow<String?> = _changePasswordResult.asStateFlow()

    fun onChangePasswordClicked() {
        val provider = settingsRepository.getCurrentAuthProvider()
        if (provider == "password") {
            _showChangePasswordDialog.value = true
        } else {
            _changePasswordResult.value = "Password change is not available for Google sign-in accounts."
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            val result = settingsRepository.changePassword(current, new)
            if (result.isSuccess) {
                _changePasswordResult.value = "Password changed successfully."
            } else {
                _changePasswordResult.value = "Failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
            _showChangePasswordDialog.value = false
        }
    }

    fun clearChangePasswordResult() {
        _changePasswordResult.value = null
    }

    fun dismissChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        clearChangePasswordResult()
        clearOperationFeedback()
    }
} 