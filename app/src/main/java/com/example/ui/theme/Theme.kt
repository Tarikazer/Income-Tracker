package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022018),
    primaryContainer = Color(0xFF0F3B30),
    onPrimaryContainer = EmeraldPrimaryLight,
    secondary = EmeraldCyan,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF133F37),
    onSecondaryContainer = Color(0xFF70F3DF),
    background = EmeraldBackground,
    onBackground = TextPrimary,
    surface = EmeraldSurface,
    onSurface = TextPrimary,
    surfaceVariant = EmeraldSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = EmeraldCardBorder,
    outlineVariant = Color(0xFF1B332B),
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = EmeraldCyanDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E48),
    background = Color(0xFFF7FBF9),
    onBackground = Color(0xFF0C1713),
    surface = Color.White,
    onSurface = Color(0xFF0C1713),
    surfaceVariant = Color(0xFFE8F3EE),
    onSurfaceVariant = Color(0xFF37544C),
    outline = Color(0xFFBDD4CB),
    outlineVariant = Color(0xFFE0ECE7),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true to match user's emerald dark UI screenshots!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
