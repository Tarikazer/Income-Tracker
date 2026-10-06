package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppStrings

// Exact palette: primary blue #2B9CE0, light blue #D3E8F8, background #EAF3FC, text navy #111827
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2B9CE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF13456B),
    onPrimaryContainer = Color(0xFFD3E8F8),
    secondary = Color(0xFFD3E8F8),
    onSecondary = Color(0xFF0D1722),
    secondaryContainer = Color(0xFF1D5A88),
    onSecondaryContainer = Color.White,
    background = Color(0xFF0D1722),
    onBackground = Color.White,
    surface = Color(0xFF142436),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1D3349),
    onSurfaceVariant = Color(0xFFA5C8E4),
    outline = Color(0xFF274460),
    outlineVariant = Color(0xFF192C3F),
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2B9CE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E8F8),
    onPrimaryContainer = Color(0xFF111827),
    secondary = Color(0xFF2B9CE0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF3FC),
    onSecondaryContainer = Color(0xFF111827),
    background = Color(0xFFEAF3FC),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F7FD),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFD3E8F8),
    outlineVariant = Color(0xFFE2EDF7),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val appStrings = AppStrings(language)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
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

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppStrings provides appStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
