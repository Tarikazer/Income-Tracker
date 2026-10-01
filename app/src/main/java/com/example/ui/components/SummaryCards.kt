package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.*
import java.util.Locale

fun formatCurrency(amount: Double, currency: String = "MAD"): String {
    return String.format(Locale.US, "%,.2f %s", amount, currency)
}

@Composable
fun HomeSpendingCard(
    totalSpent: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
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
                    text = "Total spent this month",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFFC7EAE1),
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
                text = "This only covers shopping lists, see Budget and Statistics for your full spending.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFA1CFC3),
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
    totalPlanned: Double,
    totalSpent: Double,
    plannedRemaining: Double,
    actualRemaining: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SummaryCardGradient)
            .padding(20.dp)
            .testTag("budget_summary_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BudgetRow(label = "Total income", value = totalIncome, currency = currency)
            BudgetRow(label = "Total planned", value = totalPlanned, currency = currency)
            BudgetRow(label = "Total spent", value = totalSpent, currency = currency)

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = Color(0x33FFFFFF),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            BudgetRow(
                label = "Planned remaining",
                value = plannedRemaining,
                currency = currency,
                highlightColor = EmeraldPrimaryLight,
                isBold = true
            )

            BudgetRow(
                label = "Actual remaining",
                value = actualRemaining,
                currency = currency,
                highlightColor = EmeraldCyan,
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
                color = if (isBold) highlightColor else Color(0xFFC7EAE1),
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
