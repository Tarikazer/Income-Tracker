package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==============================================================================
// Income Control Exact Palette:
// Primary Blue #2B9CE0 | Light Blue #D3E8F8 | Background #EAF3FC | Text Navy #111827
// ==============================================================================

val BrandPrimaryBlue = Color(0xFF2B9CE0)
val BrandLightBlue = Color(0xFFD3E8F8)
val BrandBackground = Color(0xFFEAF3FC)
val BrandTextNavy = Color(0xFF111827)
val BrandTextSlate = Color(0xFF475569)
val PureWhite = Color(0xFFFFFFFF)

// Standard semantic accents
val AccentAmber = Color(0xFFF59E0B)
val AccentRed = Color(0xFFEF4444)
val AccentBlue = Color(0xFF2B9CE0)
val AccentPurple = Color(0xFF8B5CF6)
val AccentPink = Color(0xFFEC4899)
val AccentGreen = Color(0xFF10B981)

@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val cardBorder: Color,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val cyan: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val dialogSurface: Color,
    val dialogBorder: Color,
    val accent: Color,
    val summaryGradient: Brush,
    val budgetGradient: Brush,
    val fabGradient: Brush
)

// Dark Theme: Ciel Blue & Deep Slate Navy
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0D1722),
    surface = Color(0xFF142436),
    surfaceElevated = Color(0xFF1D3349),
    cardBorder = Color(0xFF274460),
    primary = Color(0xFF2B9CE0),
    primaryLight = Color(0xFFD3E8F8),
    primaryDark = Color(0xFF1C80BE),
    cyan = Color(0xFF2B9CE0),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA5C8E4),
    textMuted = Color(0xFF64748B),
    buttonBackground = Color(0xFF2B9CE0),
    buttonText = Color(0xFFFFFFFF),
    dialogSurface = Color(0xFF142436),
    dialogBorder = Color(0xFF274460),
    accent = Color(0xFFD3E8F8),
    summaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF13456B),
            Color(0xFF1D5A88)
        )
    ),
    budgetGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F3B5D),
            Color(0xFF184E76)
        )
    ),
    fabGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3BA9EA),
            Color(0xFF2B9CE0)
        )
    )
)

// Light Theme: Exact Palette (#2B9CE0, #D3E8F8, #EAF3FC, #111827, White)
val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFEAF3FC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F7FD),
    cardBorder = Color(0xFFD3E8F8),
    primary = Color(0xFF2B9CE0),
    primaryLight = Color(0xFFD3E8F8),
    primaryDark = Color(0xFF1C80BE),
    cyan = Color(0xFF2B9CE0),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    buttonBackground = Color(0xFF2B9CE0),
    buttonText = Color(0xFFFFFFFF),
    dialogSurface = Color(0xFFFFFFFF),
    dialogBorder = Color(0xFFD3E8F8),
    accent = Color(0xFF176FA3),
    summaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2B9CE0),
            Color(0xFF1A7CB8)
        )
    ),
    budgetGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1F8ACD),
            Color(0xFF146DA7)
        )
    ),
    fabGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3BA9EA),
            Color(0xFF2B9CE0)
        )
    )
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// Dynamic theme accessors
val EmeraldBackground: Color @Composable get() = LocalAppColors.current.background
val EmeraldSurface: Color @Composable get() = LocalAppColors.current.surface
val EmeraldSurfaceElevated: Color @Composable get() = LocalAppColors.current.surfaceElevated
val EmeraldCardBorder: Color @Composable get() = LocalAppColors.current.cardBorder

val EmeraldPrimary: Color @Composable get() = LocalAppColors.current.primary
val EmeraldPrimaryDark: Color @Composable get() = LocalAppColors.current.primaryDark
val EmeraldPrimaryLight: Color @Composable get() = LocalAppColors.current.primaryLight
val EmeraldCyan: Color @Composable get() = LocalAppColors.current.cyan
val AccentOnSurface: Color @Composable get() = LocalAppColors.current.accent

val TextPrimary: Color @Composable get() = LocalAppColors.current.textPrimary
val TextSecondary: Color @Composable get() = LocalAppColors.current.textSecondary
val TextMuted: Color @Composable get() = LocalAppColors.current.textMuted

val SummaryCardGradient: Brush @Composable get() = LocalAppColors.current.summaryGradient
val BudgetCardGradient: Brush @Composable get() = LocalAppColors.current.budgetGradient
val AccentFabGradient: Brush @Composable get() = LocalAppColors.current.fabGradient
