package com.dummbroke.profitpath.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance, tied to the application's lifecycle
// The name "user_settings" is the name of the preferences file.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) { // Accept Context

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

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
} 