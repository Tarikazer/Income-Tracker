package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val household by viewModel.household.collectAsState()
    val monthYear by viewModel.selectedMonthYear.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val dailyPoints by viewModel.dailySpendingTrend.collectAsState()
    val categoryProgressList by viewModel.categoryProgressList.collectAsState()

    val savings = (summary.totalIncome - summary.totalSpent).coerceAtLeast(0.0)
    val savingsRate = if (summary.totalIncome > 0) ((savings / summary.totalIncome) * 100).toInt() else 0
    val dailyAverage = if (summary.totalSpent > 0) summary.totalSpent / 30.0 else 0.0

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
        containerColor = EmeraldBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("stats_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        Text(
                            text = "${household.name}'s Reports & Trends",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 19.sp
                            )
                        )
                    }

                    // Export / Share Report button
                    IconButton(
                        onClick = {
                            val report = buildString {
                                appendLine("📊 Monthly Financial Report - ${FinanceViewModel.formatMonthYearDisplay(monthYear)}")
                                appendLine("Household: ${household.name}")
                                appendLine("• Total Income: ${formatCurrency(summary.totalIncome, household.currency)}")
                                appendLine("• Total Spent: ${formatCurrency(summary.totalSpent, household.currency)}")
                                appendLine("• Net Savings: ${formatCurrency(savings, household.currency)} ($savingsRate%)")
                                appendLine("\nCategory Breakdown:")
                                categoryBreakdown.forEach {
                                    appendLine("- ${it.category.name}: ${formatCurrency(it.totalAmount, household.currency)} (${String.format(Locale.US, "%.1f", it.percentage)}%)")
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Monthly Report", report))
                            Toast.makeText(context, "Monthly report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("export_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share report",
                            tint = EmeraldPrimaryLight
                        )
                    }
                }

                // Month selector
                MonthSelector(
                    monthYear = monthYear,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onResetCurrentMonth = { viewModel.resetToCurrentMonth() }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Financial Metrics 2x2 Grid
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Net Savings",
                            value = formatCurrency(savings, household.currency),
                            subtitle = "$savingsRate% of income",
                            icon = Icons.Rounded.Savings,
                            accentColor = EmeraldPrimaryLight,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Daily Average",
                            value = formatCurrency(dailyAverage, household.currency),
                            subtitle = "Per day spend",
                            icon = Icons.Rounded.CalendarToday,
                            accentColor = EmeraldCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        MetricCard(
                            title = "Total Inflow",
                            value = formatCurrency(summary.totalIncome, household.currency),
                            subtitle = "Monthly income",
                            icon = Icons.Rounded.TrendingUp,
                            accentColor = Color(0xFF6EE7B7),
                            modifier = Modifier.weight(1f)
                        )
                        @Suppress("DEPRECATION")
                        MetricCard(
                            title = "Total Outflow",
                            value = formatCurrency(summary.totalSpent, household.currency),
                            subtitle = "All expenses",
                            icon = Icons.Rounded.TrendingDown,
                            accentColor = if (summary.totalSpent > summary.totalIncome && summary.totalIncome > 0) AccentRed else Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Interactive Donut Chart (Category Breakdown)
            item {
                InteractiveDonutChart(
                    breakdownList = categoryBreakdown,
                    currency = household.currency
                )
            }

            // Daily Spending Timeline Bar Chart
            item {
                DailySpendingTrendChart(
                    dailyPoints = dailyPoints,
                    currency = household.currency
                )
            }

            // Category Spending Breakdown
            item {
                CategorySpendingReport(
                    categoryProgress = categoryProgressList,
                    currency = household.currency
                )
            }

            // Monthly Health Insights
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSurface,
                    border = BorderStroke(1.dp, EmeraldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_insights_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Monthly Spending Insights",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        val topCategory = categoryBreakdown.firstOrNull()
                        val topCategoryNote = if (topCategory != null) {
                            "• ${topCategory.category.name} is your largest expense category (${String.format(Locale.US, "%.1f", topCategory.percentage)}% of spending)."
                        } else {
                            "• Start logging your everyday shopping to generate insights."
                        }

                        val budgetStatusNote = if (summary.actualRemaining >= 0) {
                            "• Excellent control! You are operating with ${formatCurrency(summary.actualRemaining, household.currency)} remaining surplus."
                        } else {
                            "• Caution: Total expenses exceed income by ${formatCurrency(-summary.actualRemaining, household.currency)}."
                        }

                        Text(
                            text = topCategoryNote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                        Text(
                            text = budgetStatusNote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                        Text(
                            text = "• Quick items like outside fast food and bottled drinks count under day-to-day shopping.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}
