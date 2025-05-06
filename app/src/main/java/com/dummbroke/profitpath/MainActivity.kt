package com.dummbroke.profitpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// Remove or comment out unused view imports if any remain
// import androidx.core.view.ViewCompat
// import androidx.core.view.WindowInsetsCompat
import com.dummbroke.profitpath.ui.home.HomeScreen
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

class MainActivity : ComponentActivity() { // Change to ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Often handled differently or implicitly in Compose
        setContent { // Use setContent for Compose
            ProfitPathTheme { // Apply your custom theme
                // Initially, just display the HomeScreen
                // Later, this will likely be replaced by a NavHost
                HomeScreen()
            }
        }
    }
}