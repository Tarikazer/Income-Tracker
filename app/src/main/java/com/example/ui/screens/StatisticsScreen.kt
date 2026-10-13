package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategorySpendingReport
import com.example.ui.components.DailySpendingTrendChart
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.MonthSelector
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    val household by viewModel.household.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val categoryBreakdown by viewModel.categoryExpenseBreakdown.collectAsState()
    val categoryProgress by viewModel.categoryProgressList.collectAsState()
    val dailyTrend by viewModel.dailySpendingTrend.collectAsState()

    val totalSpent = monthlySummary.totalSpent
    val totalIncome = monthlySummary.totalIncome
    val remainingBalance = monthlySummary.actualRemaining
    val expenseCount = monthlySummary.expenseCount
    val daysInMonth = 30
    val avgPerDay = if (daysInMonth > 0) totalSpent / daysInMonth else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.statsTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = strings.statsSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val summaryText = buildString {
                                appendLine("📊 Monthly Report - $selectedMonth")
                                appendLine("• Total Income: ${AppConstants.formatCurrency(totalIncome)} ${household.currency}")
                                appendLine("• Total Spent: ${AppConstants.formatCurrency(totalSpent)} ${household.currency}")
                                appendLine("• Remaining: ${AppConstants.formatCurrency(remainingBalance)} ${household.currency}")
                                appendLine("• Total Purchases: $expenseCount")
                                appendLine("• Daily Average: ${AppConstants.formatCurrency(avgPerDay)} ${household.currency}")
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Monthly Report", summaryText))
                            Toast.makeText(context, "Monthly report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_stats_button")
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy Report", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize().testTag("statistics_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthSelector(
                    monthYear = selectedMonth,
                    onPreviousMonth = { viewModel.selectPreviousMonth() },
                    onNextMonth = { viewModel.selectNextMonth() },
                    onResetCurrentMonth = { viewModel.selectCurrentMonth() }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = strings.totalSpent,
                        value = "${AppConstants.formatCurrency(totalSpent)} ${household.currency}",
                        subtitle = "$expenseCount ${strings.items}",
                        icon = Icons.Rounded.TrendingDown,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = strings.remainingBalance,
                        value = "${AppConstants.formatCurrency(remainingBalance)} ${household.currency}",
                        subtitle = "${strings.totalIncome}: ${AppConstants.formatCurrency(totalIncome)}",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        accentColor = if (remainingBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("monthly_insights_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = strings.categoryBreakdown,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        InteractiveDonutChart(
                            breakdownList = categoryBreakdown,
                            currency = household.currency,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = strings.dailyTrend,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DailySpendingTrendChart(
                            dailyPoints = dailyTrend,
                            currency = household.currency,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                CategorySpendingReport(
                    categoryProgress = categoryProgress,
                    currency = household.currency,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
