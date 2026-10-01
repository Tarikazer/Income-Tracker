package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryWithBudgetAndSpent
import com.example.ui.theme.*
import com.example.ui.viewmodel.CategoryExpenseBreakdown
import com.example.ui.viewmodel.DailySpendPoint
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveDonutChart(
    breakdownList: List<CategoryExpenseBreakdown>,
    currency: String,
    modifier: Modifier = Modifier
) {
    if (breakdownList.isEmpty()) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = EmeraldSurface,
            border = BorderStroke(1.dp, EmeraldCardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses logged for this month yet to display chart.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalAmount = remember(breakdownList) { breakdownList.sumOf { it.totalAmount } }

    val categoryColors = remember(breakdownList) {
        listOf(
            EmeraldPrimary,
            EmeraldCyan,
            AccentAmber,
            AccentBlue,
            AccentPink,
            AccentPurple,
            Color(0xFFF97316),
            Color(0xFF14B8A6)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_donut_chart"),
        shape = RoundedCornerShape(20.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Expense Breakdown by Category",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Tap any segment to inspect details",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas for Donut Chart
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .testTag("donut_canvas_box"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(breakdownList) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val diff = tapOffset - center
                                val distance = diff.getDistance()
                                val outerRadius = size.width / 2f
                                val innerRadius = outerRadius - 38.dp.toPx()

                                if (distance in innerRadius..outerRadius) {
                                    var touchAngle = Math.toDegrees(atan2(diff.y.toDouble(), diff.x.toDouble())).toFloat()
                                    if (touchAngle < 0) touchAngle += 360f

                                    // Rotate so 0 degrees starts at top (-90)
                                    var relativeAngle = (touchAngle + 90f) % 360f

                                    var currentAngle = 0f
                                    var found = false
                                    for (i in breakdownList.indices) {
                                        val sweep = (breakdownList[i].totalAmount / totalAmount).toFloat() * 360f
                                        if (relativeAngle >= currentAngle && relativeAngle <= currentAngle + sweep) {
                                            selectedIndex = if (selectedIndex == i) null else i
                                            found = true
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                    if (!found) selectedIndex = null
                                } else {
                                    selectedIndex = null
                                }
                            }
                        }
                ) {
                    val strokeWidth = 36.dp.toPx()
                    val chartRadius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    var startAngle = -90f
                    breakdownList.forEachIndexed { index, item ->
                        val sweepAngle = (item.totalAmount / totalAmount).toFloat() * 360f
                        val isSelected = selectedIndex == index
                        val color = categoryColors[index % categoryColors.size]

                        drawArc(
                            color = if (isSelected) color else color.copy(alpha = if (selectedIndex != null) 0.35f else 0.95f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            topLeft = Offset(center.x - chartRadius, center.y - chartRadius),
                            size = Size(chartRadius * 2f, chartRadius * 2f),
                            style = Stroke(
                                width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center Info Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    val activeItem = selectedIndex?.let { breakdownList.getOrNull(it) }
                    if (activeItem != null) {
                        Text(
                            text = activeItem.category.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,.1f", activeItem.totalAmount)} $currency",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", activeItem.percentage)}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = categoryColors[selectedIndex!! % categoryColors.size],
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    } else {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,.1f", totalAmount)} $currency",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Legend Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                breakdownList.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val color = categoryColors[index % categoryColors.size]

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIndex = if (selectedIndex == index) null else index
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF1E3A30) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = item.category.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", item.percentage)}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = color,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", item.totalAmount)} $currency",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailySpendingTrendChart(
    dailyPoints: List<DailySpendPoint>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val maxSpend = remember(dailyPoints) { (dailyPoints.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0) }
    var touchedPoint by remember { mutableStateOf<DailySpendPoint?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Spending Timeline",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = if (touchedPoint != null)
                            "Day ${touchedPoint!!.dayOfMonth}: ${String.format(Locale.US, "%,.2f", touchedPoint!!.amount)} $currency"
                        else
                            "Touch bars to inspect daily spend",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (touchedPoint != null) EmeraldCyan else TextSecondary,
                            fontWeight = if (touchedPoint != null) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                Text(
                    text = "Peak: ${String.format(Locale.US, "%,.0f", maxSpend)} $currency",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bar Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyPoints) {
                            detectTapGestures { offset ->
                                val step = size.width / dailyPoints.size.coerceAtLeast(1)
                                val index = (offset.x / step).toInt().coerceIn(0, dailyPoints.size - 1)
                                touchedPoint = dailyPoints.getOrNull(index)
                            }
                        }
                ) {
                    val count = dailyPoints.size
                    if (count == 0) return@Canvas
                    val spacing = 2.dp.toPx()
                    val barWidth = ((size.width - (spacing * (count - 1))) / count).coerceAtLeast(2.dp.toPx())
                    val chartHeight = size.height

                    // Draw baseline
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(0f, chartHeight),
                        end = Offset(size.width, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    dailyPoints.forEachIndexed { i, point ->
                        val barHeight = (point.amount / maxSpend).toFloat() * (chartHeight - 16.dp.toPx())
                        val x = i * (barWidth + spacing)
                        val isSelected = touchedPoint?.dayOfMonth == point.dayOfMonth

                        val barColor = when {
                            isSelected -> EmeraldCyan
                            point.amount > 0 -> EmeraldPrimary
                            else -> Color(0xFF1E3A30)
                        }

                        // Draw background slot
                        drawRoundRect(
                            color = Color(0xFF142922),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                        )

                        // Draw actual spending bar
                        if (barHeight > 0) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, chartHeight - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Day 1", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp))
                Text("Day 10", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp))
                Text("Day 20", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp))
                Text("Day 30", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp))
            }
        }
    }
}

@Composable
fun BudgetVsActualReport(
    categoryProgress: List<CategoryWithBudgetAndSpent>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_vs_actual_report"),
        shape = RoundedCornerShape(20.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Budget Adherence & Planned vs Actual",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            )
            Text(
                text = "Performance across categorized allowances",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryProgress.forEach { item ->
                    val ratio = if (item.plannedAmount > 0) item.spentAmount / item.plannedAmount else 0.0
                    val pct = (ratio * 100).toInt()
                    val statusColor = when {
                        ratio > 1.0 -> AccentRed
                        ratio > 0.85 -> AccentAmber
                        else -> EmeraldPrimary
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            )

                            Text(
                                text = "$pct% ($ratio)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { ratio.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = statusColor,
                            trackColor = Color(0xFF20372F)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${String.format(Locale.US, "%,.2f", item.spentAmount)} $currency",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "Budget: ${String.format(Locale.US, "%,.2f", item.plannedAmount)} $currency",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
