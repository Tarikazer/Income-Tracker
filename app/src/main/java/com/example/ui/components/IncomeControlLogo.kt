package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Exact color constants from user specification & attached image
val LogoPaleIceBlue = Color(0xFFEAF3FC)      // Canvas background
val LogoLightSkyBlue = Color(0xFFD3E8F8)     // Center circle & accents
val LogoLightGlowCenter = Color(0xFFE6F2FB)  // Radial glow center
val LogoPrimaryBlue = Color(0xFF2B9CE0)      // Flat solid-blue wallet
val LogoDarkNavy = Color(0xFF111827)         // Title text
val LogoSlateGray = Color(0xFF475569)        // Subtitle text

/**
 * Faithfully renders the exact "Income Control" wallet-in-circle glyph as a scalable vector.
 */
@Composable
fun IncomeControlWalletIcon(
    modifier: Modifier = Modifier,
    showBackgroundCanvas: Boolean = false,
    canvasCornerRadiusRatio: Float = 0.22f
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Optional pale ice-blue rounded canvas
        if (showBackgroundCanvas) {
            val cornerRadius = CornerRadius(s * canvasCornerRadiusRatio, s * canvasCornerRadiusRatio)
            drawRoundRect(
                color = LogoPaleIceBlue,
                topLeft = Offset((size.width - s) / 2f, (size.height - s) / 2f),
                size = Size(s, s),
                cornerRadius = cornerRadius
            )
        }

        // 2. Large centered circle in light sky blue with subtle radial glow and soft shadow
        val circleRadius = s * 0.40f
        
        // Soft drop shadow underneath
        drawCircle(
            color = Color(0x389ABFD9),
            radius = circleRadius * 1.02f,
            center = center + Offset(0f, s * 0.022f)
        )

        // Center circle with very soft radial glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LogoLightGlowCenter, LogoLightSkyBlue, Color(0xFFC7DEF3)),
                center = center - Offset(0f, circleRadius * 0.15f),
                radius = circleRadius * 1.1f
            ),
            radius = circleRadius,
            center = center
        )

        // 3. Two outlined tilted banknotes peeking out of the top of the wallet
        val noteWidth = s * 0.23f
        val noteHeight = s * 0.125f
        val strokeWidth = s * 0.022f
        val noteCorner = CornerRadius(s * 0.016f, s * 0.016f)

        // Left / Back banknote (angled ~ -38 degrees)
        withTransform({
            rotate(degrees = -38f, pivot = center + Offset(-s * 0.07f, -s * 0.11f))
        }) {
            val noteTopLeft = center + Offset(-s * 0.18f, -s * 0.17f)
            // Fill matching circle background
            drawRoundRect(
                color = LogoLightSkyBlue,
                topLeft = noteTopLeft,
                size = Size(noteWidth, noteHeight),
                cornerRadius = noteCorner
            )
            // Outline in primary blue
            drawRoundRect(
                color = LogoPrimaryBlue,
                topLeft = noteTopLeft,
                size = Size(noteWidth, noteHeight),
                cornerRadius = noteCorner,
                style = Stroke(width = strokeWidth)
            )
        }

        // Right / Front banknote (angled ~ -38 degrees with center circle)
        withTransform({
            rotate(degrees = -38f, pivot = center + Offset(s * 0.01f, -s * 0.15f))
        }) {
            val noteTopLeft = center + Offset(-s * 0.10f, -s * 0.21f)
            drawRoundRect(
                color = LogoLightSkyBlue,
                topLeft = noteTopLeft,
                size = Size(noteWidth, noteHeight),
                cornerRadius = noteCorner
            )
            drawRoundRect(
                color = LogoPrimaryBlue,
                topLeft = noteTopLeft,
                size = Size(noteWidth, noteHeight),
                cornerRadius = noteCorner,
                style = Stroke(width = strokeWidth)
            )
            // Inner circle in right banknote
            val noteCenter = noteTopLeft + Offset(noteWidth / 2f, noteHeight / 2f)
            drawCircle(
                color = LogoPrimaryBlue,
                radius = s * 0.024f,
                center = noteCenter
            )
        }

        // 4. Flat solid-blue wallet body (rounded-rectangle)
        val walletWidth = s * 0.40f
        val walletHeight = s * 0.33f
        val walletTopLeft = Offset(center.x - walletWidth * 0.51f, center.y - walletHeight * 0.22f)
        val walletCorner = CornerRadius(s * 0.055f, s * 0.055f)

        drawRoundRect(
            color = LogoPrimaryBlue,
            topLeft = walletTopLeft,
            size = Size(walletWidth, walletHeight),
            cornerRadius = walletCorner
        )

        // Thin light-blue gap / top wallet opening
        val slitStart = walletTopLeft + Offset(s * 0.035f, s * 0.038f)
        val slitEnd = walletTopLeft + Offset(walletWidth * 0.48f, s * 0.038f)
        drawLine(
            color = LogoLightSkyBlue,
            start = slitStart,
            end = slitEnd,
            strokeWidth = s * 0.014f,
            cap = StrokeCap.Round
        )

        // 5. Clasp tab on the right side
        val tabWidth = s * 0.165f
        val tabHeight = s * 0.105f
        val tabY = center.y + s * 0.02f
        val tabX = center.x + walletWidth * 0.08f
        val tabCorner = CornerRadius(s * 0.045f, s * 0.045f)

        // Draw tab body in solid blue with thin light-blue gap outline
        drawRoundRect(
            color = LogoPrimaryBlue,
            topLeft = Offset(tabX, tabY),
            size = Size(tabWidth, tabHeight),
            cornerRadius = tabCorner
        )

        // Thin light-blue gap separating clasp from wallet body
        drawRoundRect(
            color = LogoLightSkyBlue,
            topLeft = Offset(tabX, tabY),
            size = Size(tabWidth, tabHeight),
            cornerRadius = tabCorner,
            style = Stroke(width = s * 0.015f)
        )

        // Small light-blue button circle inside the clasp
        drawCircle(
            color = LogoLightSkyBlue,
            radius = s * 0.021f,
            center = Offset(tabX + tabWidth * 0.36f, tabY + tabHeight / 2f)
        )
    }
}

/**
 * Small version of the wallet-in-circle for the app top bar / header.
 */
@Composable
fun IncomeControlHeaderLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IncomeControlWalletIcon(
            modifier = Modifier.fillMaxSize(),
            showBackgroundCanvas = false
        )
    }
}

/**
 * Full splash / loading screen version matching user's exact uploaded image:
 * - Square canvas with pale ice-blue background (#EAF3FC)
 * - "Income Control" title in bold navy (#111827) above the circle
 * - Centered large circle in light sky blue (#D3E8F8) with the flat blue wallet
 * - "Smart Budget & Spending" subtitle in slate gray (#475569) below the title
 */
@Composable
fun IncomeControlSplashLogo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title Text above the circle matching image
        Text(
            text = "Income Control",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = LogoDarkNavy,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle on splash screen
        Text(
            text = "Smart Budget & Spending",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LogoSlateGray,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Center circle with wallet glyph
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            IncomeControlWalletIcon(
                modifier = Modifier.fillMaxSize(),
                showBackgroundCanvas = false
            )
        }
    }
}
