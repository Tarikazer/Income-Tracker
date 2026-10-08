package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.CategoryWithSpent
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import java.util.Locale

@Composable
fun CategoryCard(
    categoryData: CategoryWithSpent,
    currency: String,
    onAddExpense: () -> Unit,
    onViewHistory: () -> Unit,
    onReorder: () -> Unit,
    onDeleteCategory: () -> Unit,
    coverage: com.example.data.model.CategoryCoverage? = null,
    modifier: Modifier = Modifier
) {
    val category = categoryData.category
    val spent = categoryData.spentAmount
    val strings = LocalAppStrings.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewHistory() }
            .testTag("category_card_${category.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder)
    ) {
        Column(
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
                .padding(16.dp)
        ) {
            // Header Row: Icon, Name, Action icons (History, Reorder, Delete)
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

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${categoryData.expensesCount} transaction${if (categoryData.expensesCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Actions: History, Reorder (Up/Down Swap), Delete Category
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

                    // Up/Down Swap icon to reorder categories
                    IconButton(
                        onClick = onReorder,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reorder_category_${category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Reorder category up/down",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
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

            if (coverage != null) {
                val endFormatted = com.example.util.AppConstants.formatMonthYear(coverage.endMonthYear, strings.isFrench)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("already_paid_badge_${category.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.alreadyPaidCoveredUntil(endFormatted),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (coverage != null) 4.dp else 14.dp))

            // Bottom Row: Total spent on left, + Add expense pill button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total spent",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", spent)} $currency",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag("spent_text_${category.id}")
                    )
                }

                // "+ Add expense" pill button
                val isDark = LocalAppColors.current.isDark
                val pillContainerColor = if (isDark) Color(0xFF18382C) else EmeraldPrimary
                val pillBorderColor = if (isDark) Color(0xFF285442) else EmeraldPrimaryDark
                val pillContentColor = if (isDark) EmeraldPrimaryLight else Color.White

                Surface(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(20.dp),
                    color = pillContainerColor,
                    border = BorderStroke(1.dp, pillBorderColor),
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
                            tint = pillContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add expense",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = pillContentColor,
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
