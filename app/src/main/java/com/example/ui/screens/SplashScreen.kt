package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IncomeControlWalletIcon
import com.example.ui.components.LogoDarkNavy
import com.example.ui.components.LogoPaleIceBlue
import com.example.ui.components.LogoSlateGray

@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LogoPaleIceBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Title Text: "Income Control" centered above the circle
            Text(
                text = "Income Control",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LogoDarkNavy,
                    fontSize = 34.sp,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: "Smart Budget & Spending" below title in slate gray
            Text(
                text = "Smart Budget & Spending",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LogoSlateGray,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Large centered circle with exact wallet glyph
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                IncomeControlWalletIcon(
                    modifier = Modifier.fillMaxSize(),
                    showBackgroundCanvas = false
                )
            }
        }
    }
}
