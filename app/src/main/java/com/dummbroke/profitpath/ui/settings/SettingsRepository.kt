package com.dummbroke.profitpath.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.Timestamp
import java.time.LocalDate

// Create a DataStore instance, tied to the application's lifecycle
// The name "user_settings" is the name of the preferences file.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

// User Profile Data Class
data class UserProfile(
    val email: String = "",
    val name: String = "Trader",
    val tradingStyle: String = "day_trader", // Default style
    val currentBalance: Double = 0.0
)

class SettingsRepository(private val context: Context) { // Accept Context

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    private fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    fun getUserEmail(): String? = firebaseAuth.currentUser?.email

    fun getUserProfile(): Flow<UserProfile?> = flow {
        val userId = getCurrentUserId()
        if (userId == null) {
            val email = getUserEmail() ?: ""
            val name = firebaseAuth.currentUser?.displayName ?: email.substringBefore('@').ifBlank { "Trader" }
            emit(UserProfile(email = email, name = name))
            return@flow
        }
        val docRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            val profile = snapshot.toObject(UserProfile::class.java)
            val email = getUserEmail() ?: profile?.email ?: ""
            val name = profile?.name ?: firebaseAuth.currentUser?.displayName ?: email.substringBefore('@').ifBlank { "Trader" }
            emit(profile?.copy(email = email, name = name))
        } else {
            val email = getUserEmail() ?: ""
            val name = firebaseAuth.currentUser?.displayName ?: email.substringBefore('@').ifBlank { "Trader" }
            emit(UserProfile(email = email, name = name, currentBalance = 0.0))
        }
    }.catch { e ->
        val email = getUserEmail() ?: ""
        val name = firebaseAuth.currentUser?.displayName ?: email.substringBefore('@').ifBlank { "Trader" }
        emit(UserProfile(email = email, name = name, currentBalance = 0.0))
    }

    suspend fun updateDisplayName(name: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) return Result.failure(Exception("User not logged in"))
            val profileDocRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
            profileDocRef.set(
                mapOf(
                    "name" to name,
                    "lastSynced" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTradingStyle(styleId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) return Result.failure(Exception("User not logged in"))
            val profileDocRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
            profileDocRef.set(
                mapOf(
                    "tradingStyle" to styleId,
                    "lastSynced" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurrentBalance(balance: Double): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) return Result.failure(Exception("User not logged in"))
            val profileDocRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
            // Ensure balance is not negative, or handle as per requirements
            val validBalance = if (balance < 0) 0.0 else balance
            profileDocRef.set(
                mapOf(
                    "currentBalance" to validBalance,
                    "lastSynced" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Theme Preference
    suspend fun setThemePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    fun getThemePreference(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                // Default to true (dark mode) if the preference is not set
                preferences[PreferencesKeys.IS_DARK_MODE] ?: true
            }
    }

    suspend fun deleteCurrentUserAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            user?.delete()
            // Optionally, delete Firestore user data here
            val userId = getCurrentUserId()
            if (userId != null) {
                firestore.collection("users").document(userId).delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearLocalImageCache(): Result<Unit> {
        return try {
            val dir = File("/storage/emulated/0/TradingJournal/")
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { it.delete() }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCloudSyncStatus(): Flow<String> {
        return flow {
            val userId = getCurrentUserId()
            if (userId == null) {
                emit("Not synced")
                return@flow
            }
            val docRef = firestore.collection("users").document(userId).collection("profile").document("user_profile_data")
            val snapshot = docRef.get().await()
            val lastSynced = snapshot.getTimestamp("lastSynced")
            if (lastSynced != null) {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastSynced.toDate())
                emit("Last synced: $date")
            } else {
                emit("Last synced: Never")
            }
        }.catch { emit("Sync error") }
    }

    fun getCurrentAuthProvider(): String? {
        return FirebaseAuth.getInstance().currentUser?.providerData?.lastOrNull()?.providerId
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.failure(Exception("No user"))
        val email = user.email ?: return Result.failure(Exception("No email"))
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
        return try {
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 