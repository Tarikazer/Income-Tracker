package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.example.util.AppConstants
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseItemRow(
    expense: ExpenseEntity,
    currency: String,
    onItemClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = try {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(expense.dateTimestamp))
    } catch (e: Exception) {
        ""
    }

    // 24-hour edit/delete window based on stored creation timestamp for all items
    val isEditable = AppConstants.isExpenseEditable(expense.dateTimestamp)

    // Never display "Alimentation" for separate shopping items on Home Screen
    val showCategory = expense.categoryName.isNotBlank() &&
            !expense.categoryName.equals("Alimentation", ignoreCase = true) &&
            !expense.categoryName.equals("Separate Purchase", ignoreCase = true)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (isEditable) onEditClick() else onItemClick()
            }
            .testTag("expense_row_${expense.id}"),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF11221C),
        border = BorderStroke(1.dp, Color(0xFF1D382E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF142720),
                            Color(0xFF0F1E19)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B3D30)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getCategoryIcon(if (expense.categoryIconKey.isBlank() || expense.categoryIconKey == "alimentation") "shopping" else expense.categoryIconKey),
                        contentDescription = expense.title,
                        tint = EmeraldPrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        )
                        if (expense.isRecurring) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = "Recurring expense",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showCategory) {
                            Text(
                                text = expense.categoryName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = if (expense.note.isNotBlank()) expense.note else dateStr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "- ${String.format(Locale.US, "%,.2f", expense.amount)} $currency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("expense_amount_${expense.id}")
                )

                // 24-hour window for ALL items: within 24h shows Edit & Delete buttons; after 24h shows Lock icon only
                if (isEditable) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_expense_${expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit item (24h window)",
                            tint = EmeraldPrimaryLight,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_expense_${expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete expense",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Editing and deletion locked after 24h",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
