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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
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

    // Do not display category if it's generic shopping or identical to the item title
    val showCategory = expense.categoryName.isNotBlank() &&
            !expense.categoryName.equals("Alimentation", ignoreCase = true) &&
            !expense.categoryName.equals("Separate Purchase", ignoreCase = true) &&
            !expense.categoryName.equals(expense.title, ignoreCase = true)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (isEditable) onEditClick() else onItemClick()
            }
            .testTag("expense_row_${expense.id}"),
        shape = RoundedCornerShape(18.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmeraldSurface,
                            EmeraldSurfaceElevated
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category / Item icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconHelper.getCategoryIcon(if (expense.categoryIconKey.isBlank() || expense.categoryIconKey == "alimentation") "shopping" else expense.categoryIconKey),
                    contentDescription = expense.title,
                    tint = AccentOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Subtitle column (expands flexibly)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (expense.coversMonths > 1) {
                        val strings = LocalAppStrings.current
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("expense_covers_chip_${expense.id}")
                        ) {
                            Text(
                                text = strings.monthsBadge(expense.coversMonths),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentOnSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (expense.isRecurring) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Recurring expense",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (showCategory) {
                        Text(
                            text = expense.categoryName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "- ${String.format(Locale.US, "%,.2f", expense.amount)} $currency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .testTag("expense_amount_${expense.id}")
                )

                // 24-hour window for ALL items: within 24h shows Edit & Delete buttons; after 24h shows Lock icon only
                if (isEditable) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_expense_${expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit item (24h window)",
                            tint = AccentOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_expense_${expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete expense",
                            tint = TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Editing and deletion locked after 24h",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
