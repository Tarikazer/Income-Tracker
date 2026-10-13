package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IncomeControlWalletIcon(
    modifier: Modifier = Modifier,
    showBackgroundCanvas: Boolean = true,
    canvasCornerRadiusRatio: Float = 0.28f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (showBackgroundCanvas) {
            drawRoundRect(
                color = Color(0xFFD3E8F8),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * canvasCornerRadiusRatio, h * canvasCornerRadiusRatio)
            )
        }
        val walletColor = Color(0xFF2B9CE0)
        drawRoundRect(
            color = walletColor,
            topLeft = Offset(w * 0.18f, h * 0.28f),
            size = Size(w * 0.64f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
        )
        drawCircle(
            color = Color(0xFFEAF3FC),
            radius = w * 0.05f,
            center = Offset(w * 0.68f, h * 0.52f)
        )
    }
}

@Composable
fun IncomeControlHeaderLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    IncomeControlWalletIcon(modifier = modifier.size(size))
}

@Composable
fun IncomeControlSplashLogo(
    modifier: Modifier = Modifier
) {
    IncomeControlWalletIcon(modifier = modifier.size(108.dp))
}
