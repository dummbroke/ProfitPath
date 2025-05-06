package com.dummbroke.profitpath.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define placeholder colors inspired by TradingView dark theme
// TODO: Replace with actual TradingView palette
private val DarkBackground = Color(0xFF1E222D) // Dark Gray/Blue
private val DarkSurface = Color(0xFF2A2E39) // Slightly Lighter Dark
private val DarkOnBackground = Color.White
private val DarkOnSurface = Color(0xFFB2B5BE) // Light Gray Text
private val AccentGreen = Color(0xFF26A69A) // Tealish Green (Win)
private val AccentRed = Color(0xFFEF5350)   // Red (Loss)

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,           // Primary actions, maybe green
    secondary = AccentRed,         // Secondary accent, maybe red
    tertiary = Color.Gray,           // Neutral accent
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = AccentRed,
    onError = Color.Black
    // Define other colors as needed (surfaceVariant, inverseSurface, etc.)
)

// Define placeholder colors for light theme
// TODO: Define a proper light theme palette if needed
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB00020),
    onError = Color.White
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ProfitPathTheme(
    darkTheme: Boolean = true, // Default to dark theme
    // Dynamic color is available on Android 12+ but might not fit TradingView style
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Allow toggling later
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Use background for status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography.kt exists or will be created
        content = content
    )
} 