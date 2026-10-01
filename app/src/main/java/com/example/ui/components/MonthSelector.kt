package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun MonthSelector(
    monthYear: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayLabel = FinanceViewModel.formatMonthYearDisplay(monthYear)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(48.dp)
                .testTag("prev_month_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = displayLabel,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = TextPrimary
            ),
            modifier = Modifier
                .clickable { onResetCurrentMonth() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("month_display_text")
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(48.dp)
                .testTag("next_month_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = TextPrimary
            )
        }
    }
}
