package com.dummbroke.profitpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import for by viewModels()
import androidx.compose.runtime.collectAsState // Import for collectAsState
import androidx.compose.runtime.getValue // Import for by getValue
// import com.dummbroke.profitpath.ui.home.HomeScreen // No longer directly called here
import com.dummbroke.profitpath.ui.navigation.AppNavigation // Import AppNavigation
import com.dummbroke.profitpath.ui.settings.SettingsViewModel // Import SettingsViewModel
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    // Get a ViewModel instance scoped to this Activity
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        // enableEdgeToEdge() // Often handled differently or implicitly in Compose
        setContent { // Use setContent for Compose
            // Observe the dark mode state from the ViewModel
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            ProfitPathTheme(darkTheme = isDarkMode) { // Apply your custom theme with dynamic darkTheme value
                AppNavigation() // Call AppNavigation here
            }
        }
    }
}