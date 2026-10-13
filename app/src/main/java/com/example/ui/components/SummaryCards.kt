package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeriodSpending
import com.example.data.model.SwipeableSpendingSummary
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.LocalAppStrings
import com.example.util.AppConstants

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableHomeSpendingCard(
    summary: SwipeableSpendingSummary,
    currency: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current
    val pagerState = rememberPagerState(
        initialPage = summary.currentPageIndex.coerceIn(0, (summary.pages.size - 1).coerceAtLeast(0)),
        pageCount = { summary.pages.size }
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_spending_summary_card"),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val page = summary.pages.getOrNull(pageIndex)
                if (page != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = page.periodLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.primary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${AppConstants.formatCurrency(page.currentAmount)} $currency",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (AppConstants.shouldShowComparison(page.previousAmount)) {
                            val diff = page.currentAmount - page.previousAmount
                            val sign = if (diff >= 0.0) "+" else ""
                            val diffText = "$sign${AppConstants.formatCurrency(diff)} $currency"
                            Text(
                                text = "vs previous: $diffText",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (diff <= 0.0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            if (summary.pages.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(summary.pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) colors.primary else colors.textSecondary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}
