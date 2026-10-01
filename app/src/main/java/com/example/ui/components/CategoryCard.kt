package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.example.data.model.CategoryWithBudgetAndSpent
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CategoryCard(
    categoryData: CategoryWithBudgetAndSpent,
    currency: String,
    onAddExpense: () -> Unit,
    onEditBudget: () -> Unit,
    onViewHistory: () -> Unit,
    onDeleteCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = categoryData.category
    val planned = categoryData.plannedAmount
    val spent = categoryData.spentAmount

    val progressFraction = if (planned > 0) {
        (spent / planned).toFloat().coerceIn(0f, 1f)
    } else if (spent > 0) {
        1f
    } else {
        0f
    }

    val progressColor = when {
        spent > planned && planned > 0 -> AccentRed
        progressFraction > 0.85f -> AccentAmber
        else -> EmeraldPrimary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_card_${category.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF11221C),
        border = BorderStroke(1.dp, Color(0xFF1D382E))
    ) {
        Column(
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
                .padding(16.dp)
        ) {
            // Header Row: Icon, Name, (Recurring icon), and Action icons (History, Edit/Swap, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(category.colorHex).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getCategoryIcon(category.iconKey),
                            contentDescription = category.name,
                            tint = Color(category.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    )

                    if (category.isRecurring) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Recurring",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Actions: History, Edit Budget, Delete Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onViewHistory,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("view_history_${category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Category history",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditBudget,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_budget_${category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Edit budget",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteCategory,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_category_${category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete category",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = Color(0xFF1E332B),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row: Spent / Planned on left, + Add expense pill button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale.US, "%,.2f", spent)} / ${String.format(Locale.US, "%,.2f", planned)} $currency",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.testTag("budget_progress_text_${category.id}")
                )

                // "+ Add expense" pill button (matching Screenshot 2 exactly)
                Surface(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF18382C),
                    border = BorderStroke(1.dp, Color(0xFF285442)),
                    modifier = Modifier.testTag("add_expense_btn_${category.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = EmeraldPrimaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add expense",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldPrimaryLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
