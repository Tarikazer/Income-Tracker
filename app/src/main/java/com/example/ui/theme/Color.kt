package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Emerald Palette matching screenshots
val EmeraldBackground = Color(0xFF0C1713)
val EmeraldSurface = Color(0xFF13231E)
val EmeraldSurfaceElevated = Color(0xFF192F28)
val EmeraldCardBorder = Color(0xFF1F3A32)

val EmeraldPrimary = Color(0xFF10B981)
val EmeraldPrimaryDark = Color(0xFF059669)
val EmeraldPrimaryLight = Color(0xFF34D399)

val EmeraldCyan = Color(0xFF2DD4BF)
val EmeraldCyanDark = Color(0xFF0D9488)

val TextPrimary = Color(0xFFF1F6F4)
val TextSecondary = Color(0xFF8FAEA4)
val TextMuted = Color(0xFF5F7E75)

val AccentAmber = Color(0xFFF59E0B)
val AccentRed = Color(0xFFEF4444)
val AccentBlue = Color(0xFF3B82F6)
val AccentPurple = Color(0xFF8B5CF6)
val AccentPink = Color(0xFFEC4899)

// Gradients
val SummaryCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF094335),
        Color(0xFF114F40)
    )
)

val BudgetCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A3E32),
        Color(0xFF0D4B3D)
    )
)

val AccentFabGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF059669)
    )
)
