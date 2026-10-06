package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SwipeableSpendingSummary
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import java.util.Locale
import kotlin.math.abs

fun formatCurrency(amount: Double, currency: String = "MAD"): String {
    return String.format(Locale.US, "%,.2f %s", amount, currency)
}

@Composable
fun SwipeableHomeSpendingCard(
    summary: SwipeableSpendingSummary,
    currency: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val pages = listOf(
        summary.monthSpending,
        summary.weekSpending,
        summary.todaySpending
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SummaryCardGradient)
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag("swipeable_home_spending_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                val displayTitle = when {
                    page.title.contains("month", ignoreCase = true) -> strings.totalSpentThisMonth
                    page.title.contains("week", ignoreCase = true) -> strings.totalSpentThisWeek
                    page.title.contains("today", ignoreCase = true) -> strings.totalSpentToday
                    else -> page.title
                }

                val displayPeriodLabel = when {
                    page.periodLabel.contains("month", ignoreCase = true) -> strings.thanLastMonth
                    page.periodLabel.contains("week", ignoreCase = true) -> strings.thanLastWeek
                    page.periodLabel.contains("yesterday", ignoreCase = true) -> strings.thanYesterday
                    else -> page.periodLabel
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFFE0F2FE),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp
                            )
                        )

                        Text(
                            text = formatCurrency(page.currentAmount, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            modifier = Modifier.testTag("spending_amount_page_$pageIndex")
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Comparison vs previous period: arrow + color
                    val diff = page.diffAmount
                    val absDiff = abs(diff)
                    val formattedDiff = formatCurrency(absDiff, currency)

                    // If last month's total spending was 0, hide or do not display the text that shows the difference compared to last month
                    val isLastMonthComparison = page.periodLabel.contains("last month", ignoreCase = true) ||
                            page.title.contains("month", ignoreCase = true)
                    val hideComparison = isLastMonthComparison && page.previousAmount <= 0.0001

                    if (!hideComparison) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (diff > 0.009) {
                                // Spent MORE than previous period: Red with ▲
                                Text(
                                    text = "▲ $formattedDiff ${strings.more} $displayPeriodLabel",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFF6B6B),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            } else if (diff < -0.009) {
                                // Spent LESS than previous period: Bright sky green with ▼
                                Text(
                                    text = "▼ $formattedDiff ${strings.less} $displayPeriodLabel",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF7DD3FC),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            } else {
                                Text(
                                    text = strings.equalToPreviousPeriod,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFBAE6FD),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }

                    Text(
                        text = strings.shoppingListPurchasesOnly,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xCCBAE6FD),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Page indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(
                                width = if (isSelected) 14.dp else 6.dp,
                                height = 6.dp
                            )
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) Color(0xFF38BDF8) else Color(0x66BAE6FD))
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSpendingCard(
    totalSpent: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SummaryCardGradient)
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag("home_spending_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.totalSpentThisMonth,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFFE0F2FE),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp
                    )
                )

                Text(
                    text = formatCurrency(totalSpent, currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.testTag("home_total_spent_text")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.shoppingListPurchasesOnly,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xCCBAE6FD),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}

@Composable
fun BudgetSummaryCard(
    totalIncome: Double,
    totalSpent: Double,
    actualRemaining: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(BudgetCardGradient)
            .padding(20.dp)
            .testTag("budget_summary_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BudgetRow(label = strings.totalIncome, value = totalIncome, currency = currency)
            BudgetRow(label = strings.totalSpent, value = totalSpent, currency = currency)

            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(
                color = Color(0x33FFFFFF),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            BudgetRow(
                label = strings.remainingBalance,
                value = actualRemaining,
                currency = currency,
                highlightColor = if (actualRemaining >= 0) Color(0xFF7DD3FC) else AccentRed,
                isBold = true
            )
        }
    }
}

@Composable
private fun BudgetRow(
    label: String,
    value: Double,
    currency: String,
    highlightColor: Color = Color.White,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isBold) highlightColor else Color(0xFFE0F2FE),
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = if (isBold) 15.sp else 14.sp
            )
        )

        Text(
            text = formatCurrency(value, currency),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = highlightColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                fontSize = if (isBold) 16.sp else 14.sp
            )
        )
    }
}
