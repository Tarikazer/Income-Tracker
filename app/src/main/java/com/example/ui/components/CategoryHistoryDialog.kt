package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryCoverage
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import com.example.util.AppConstants

@Composable
fun CategoryHistoryDialog(
    category: CategoryEntity,
    expenses: List<ExpenseEntity>,
    currency: String,
    onDismiss: () -> Unit,
    coverage: CategoryCoverage? = null,
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = EmeraldSurface,
            border = BorderStroke(1.dp, EmeraldCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("category_history_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = IconHelper.getCategoryIcon(category.iconKey),
                            contentDescription = null,
                            tint = Color(category.colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${category.name} Expenses",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (expenses.isEmpty()) {
                    if (coverage != null) {
                        val coveringExp = coverage.coveringExpense
                        val paidMonthFormatted = AppConstants.formatMonthYear(coveringExp.monthYear, strings.isFrench)
                        val amountStr = if (coveringExp.amount == coveringExp.amount.toLong().toDouble())
                            coveringExp.amount.toLong().toString()
                        else
                            String.format(java.util.Locale.US, "%.2f", coveringExp.amount)
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .testTag("covered_category_history_info")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = strings.coveredByPaidIn(amountStr, currency, paidMonthFormatted),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.alreadyPaidCoveredUntil(AppConstants.formatMonthYear(coverage.endMonthYear, strings.isFrench)),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses logged in ${category.name} this month.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        items(expenses) { expense ->
                            val isEditable = AppConstants.isExpenseEditable(expense.dateTimestamp)
                            ExpenseItemRow(
                                expense = expense,
                                currency = currency,
                                onItemClick = {
                                    if (isEditable) {
                                        onEditExpense(expense)
                                    }
                                },
                                onEditClick = {
                                    if (isEditable) {
                                        onEditExpense(expense)
                                    }
                                },
                                onDeleteClick = {
                                    if (isEditable) {
                                        onDeleteExpense(expense)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
