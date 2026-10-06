package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.example.util.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Ciel Blue (Sky Blue) & White dialog theme values
val MintButtonColor: Color @Composable get() = LocalAppColors.current.buttonBackground
val MintButtonTextColor: Color @Composable get() = LocalAppColors.current.buttonText
val DialogSurfaceColor: Color @Composable get() = LocalAppColors.current.dialogSurface
val DialogBorderColor: Color @Composable get() = LocalAppColors.current.dialogBorder

/**
 * Add Income Dialog matching user's Screenshot 1:
 * Clean, minimalistic with only:
 * - "Add income" title
 * - "MAD" outlined text field
 * - "Cancel" and "Save" (mint button)
 */
@Composable
fun AddIncomeDialog(
    currency: String,
    existingIncomesCount: Int = 0,
    onDismiss: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isFirstIncome = existingIncomesCount == 0
    val incomeCategoryLabel = if (isFirstIncome) "Monthly Income" else "Other Incomes"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("add_income_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add income",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFirstIncome) Color(0xFF1B3D30) else Color(0xFF1E353B),
                        border = BorderStroke(1.dp, if (isFirstIncome) Color(0xFF2C634F) else Color(0xFF2E535C))
                    ) {
                        Text(
                            text = incomeCategoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFirstIncome) Color(0xFF7DE0BA) else Color(0xFF67E8F9),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFirstIncome)
                        "This will be recorded as your main Monthly Income."
                    else
                        "This will be recorded and labeled as Other Incomes.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_amount_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("income_cancel_button")
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMessage = "Please enter a valid amount."
                                return@Button
                            }
                            onSave(amount)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintButtonColor,
                            contentColor = MintButtonTextColor
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("save_income_button")
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add Category Expense Dialog matching user's Screenshot 2:
 * Used when user taps "+ Add expense" on a category card (e.g. Rent, Sport, Water Bill):
 * - "Add expense" title
 * - "MAD" outlined text field
 * - "Added to this category's total spending" subtext
 * - "Note (optional)" text field
 * - "Cancel" and "Save" (mint button)
 */
@Composable
fun AddCategoryExpenseDialog(
    categoryName: String,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("add_category_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Add expense",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Added to this category's total spending",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Note (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("expense_cancel_button")
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMessage = "Please enter a valid amount."
                                return@Button
                            }
                            onSave(amount, noteText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintButtonColor,
                            contentColor = MintButtonTextColor
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("save_category_expense_button")
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add Shopping Expense Dialog for the Home Screen:
 * Clean, fast dialog for separate everyday items you buy (water bottles, fast food, supermarket groceries)
 * without category selection.
 */
@Composable
fun AddShoppingExpenseDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, note: String, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("add_shopping_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Add expense",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("What did you buy? (e.g. Water bottle, Fast food)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_title_input")
                )

                // Quick item suggestion chips with compact wrapping flow
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val suggestions = listOf(
                        "Breakfast",
                        "Lunch",
                        "Dinner",
                        "Drinks",
                        "Supermarket",
                        "Water bottle",
                        "Coffee",
                        "Fast food",
                        "Groceries",
                        "Snacks"
                    )
                    suggestions.forEach { suggestion ->
                        val isSelected = title.equals(suggestion, ignoreCase = true)
                        Surface(
                            onClick = {
                                title = suggestion
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MintButtonColor else EmeraldSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MintButtonColor else EmeraldCardBorder
                            )
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) MintButtonTextColor else TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                softWrap = true,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_amount_input")
                )

                // Date & Time Picker Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date picker button
                        Surface(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val updatedCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                        updatedCal.set(Calendar.YEAR, year)
                                        updatedCal.set(Calendar.MONTH, month)
                                        updatedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        val now = System.currentTimeMillis()
                                        val newTime = updatedCal.timeInMillis
                                        selectedTimestamp = if (newTime > now) now else newTime
                                        errorMessage = null
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                )
                                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                                datePickerDialog.show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSurfaceElevated,
                            border = BorderStroke(1.dp, EmeraldCardBorder),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("pick_purchase_date_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = "Pick date",
                                    tint = MintButtonColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Date",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = dateFormat.format(Date(selectedTimestamp)),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Time picker button
                        Surface(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                val timePickerDialog = android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val updatedCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                                        updatedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        updatedCal.set(Calendar.MINUTE, minute)
                                        updatedCal.set(Calendar.SECOND, 0)
                                        val now = System.currentTimeMillis()
                                        val newTime = updatedCal.timeInMillis
                                        if (newTime > now) {
                                            errorMessage = "Future date/time is not allowed."
                                        } else {
                                            selectedTimestamp = newTime
                                            errorMessage = null
                                        }
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                )
                                timePickerDialog.show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSurfaceElevated,
                            border = BorderStroke(1.dp, EmeraldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_purchase_time_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Pick time",
                                    tint = MintButtonColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Time",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = timeFormat.format(Date(selectedTimestamp)),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val isBackdated24h = (System.currentTimeMillis() - selectedTimestamp) > (24 * 3600 * 1000L)
                    if (isBackdated24h) {
                        Text(
                            text = "⚠️ Backdated by >24h: purchase will be locked upon saving.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Note (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_note_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanAmount = amountText.replace(',', '.').trim().toDoubleOrNull()
                            if (title.isBlank()) {
                                errorMessage = "Please enter or pick what you bought."
                                return@Button
                            }
                            if (cleanAmount == null || cleanAmount <= 0) {
                                errorMessage = "Please enter a valid amount (e.g. 25.50)."
                                return@Button
                            }
                            if (selectedTimestamp > System.currentTimeMillis()) {
                                errorMessage = "Future date/time is not allowed."
                                return@Button
                            }
                            onSave(title.trim(), cleanAmount, note.trim(), selectedTimestamp)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintButtonColor,
                            contentColor = MintButtonTextColor
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("save_shopping_expense_button")
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HouseholdDialog(
    currentName: String,
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, currency: String) -> Unit,
    onOpenBackupRestore: () -> Unit = {}
) {
    var name by remember { mutableStateOf(currentName) }
    var currency by remember { mutableStateOf(currentCurrency) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("household_dialog")
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Household Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Household Name (e.g. Tarik)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency Code (e.g. MAD, EUR, USD)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household_currency_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenBackupRestore()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF284C3E)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MintButtonColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_backup_restore_from_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsBackupRestore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Backup & Restore", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && currency.isNotBlank()) {
                                onSave(name, currency)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintButtonColor,
                            contentColor = MintButtonTextColor
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("save_household_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Local Backup and Restore Dialog using Android Storage Access Framework (SAF).
 * Allows exporting app data to a local file and restoring it back. No cloud services.
 */
@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    statusMessage: String? = null,
    onClearStatus: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
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
                            imageVector = Icons.Rounded.SettingsBackupRestore,
                            contentDescription = null,
                            tint = MintButtonColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Backup & Restore",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 20.sp
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Export your app database (all purchases, categories, incomes, and settings) to a local file on your device via Storage Access Framework, or restore from a previously saved backup file. 100% offline & private.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 17.sp,
                        fontSize = 12.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Card 1: Backup (Export)
                Surface(
                    onClick = onExportBackup,
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldSurfaceElevated,
                    border = BorderStroke(1.dp, EmeraldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.UploadFile,
                                contentDescription = null,
                                tint = MintButtonColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Local Backup",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Save a .json backup to your device storage",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Card 2: Restore (Import)
                Surface(
                    onClick = onImportBackup,
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldSurfaceElevated,
                    border = BorderStroke(1.dp, EmeraldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_backup_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DownloadForOffline,
                                contentDescription = null,
                                tint = MintButtonColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restore from Local File",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Select an existing backup file to restore",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldSurfaceElevated,
                        border = BorderStroke(1.dp, MintButtonColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearStatus, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun EditShoppingExpenseDialog(
    expense: ExpenseEntity,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, note: String) -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var amountText by remember {
        mutableStateOf(
            if (expense.amount == expense.amount.toLong().toDouble())
                expense.amount.toLong().toString()
            else
                String.format(java.util.Locale.US, "%.2f", expense.amount)
        )
    }
    var note by remember { mutableStateOf(expense.note) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val remainingHours = AppConstants.remainingHours(expense.dateTimestamp)
    val isEditable = AppConstants.isExpenseEditable(expense.dateTimestamp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("edit_shopping_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = if (isEditable) "$remainingHours h left to edit" else "Locked (>24h)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isEditable) EmeraldCyan else Color(0xFFFBBF24),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Title / Item Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedLabelColor = MintButtonColor,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Read-only Date & Time section (disabled fields)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Date & Time (Read-only)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateFormat.format(Date(expense.dateTimestamp)),
                            onValueChange = {},
                            enabled = false,
                            readOnly = true,
                            label = { Text("Date") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Read-only date",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextSecondary,
                                disabledBorderColor = Color(0xFF284C3E).copy(alpha = 0.6f),
                                disabledLabelColor = TextMuted,
                                disabledLeadingIconColor = TextMuted,
                                disabledTrailingIconColor = TextMuted,
                                disabledContainerColor = Color(0xFF101E1A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("edit_expense_date_readonly")
                        )

                        OutlinedTextField(
                            value = timeFormat.format(Date(expense.dateTimestamp)),
                            onValueChange = {},
                            enabled = false,
                            readOnly = true,
                            label = { Text("Time") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Read-only time",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextSecondary,
                                disabledBorderColor = Color(0xFF284C3E).copy(alpha = 0.6f),
                                disabledLabelColor = TextMuted,
                                disabledLeadingIconColor = TextMuted,
                                disabledTrailingIconColor = TextMuted,
                                disabledContainerColor = Color(0xFF101E1A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_expense_time_readonly")
                        )
                    }

                    Text(
                        text = "Date and time can only be set when created and cannot be edited.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Note (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintButtonColor,
                        unfocusedBorderColor = Color(0xFF354B42),
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                        cursorColor = MintButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (!isEditable) {
                                errorMessage = "This transaction was backdated or is older than 24 hours and cannot be edited."
                                return@Button
                            }
                            val cleanAmount = amountText.replace(',', '.').trim().toDoubleOrNull()
                            if (title.isBlank()) {
                                errorMessage = "Please enter an item name."
                                return@Button
                            }
                            if (cleanAmount == null || cleanAmount <= 0) {
                                errorMessage = "Please enter a valid amount."
                                return@Button
                            }
                            onSave(title.trim(), cleanAmount, note.trim())
                        },
                        enabled = isEditable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintButtonColor,
                            contentColor = MintButtonTextColor,
                            disabledContainerColor = EmeraldSurfaceElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(if (isEditable) "Update" else "Locked", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReorderCategoryDialog(
    category: CategoryEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DialogSurfaceColor,
            border = BorderStroke(1.dp, DialogBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("reorder_category_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Reorder ${category.name}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Move this category up or down in your budget list:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onMoveUp()
                            onDismiss()
                        },
                        enabled = canMoveUp,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSurfaceElevated,
                            contentColor = EmeraldPrimary,
                            disabledContainerColor = EmeraldCardBorder.copy(alpha = 0.5f),
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Move Up")
                    }

                    Button(
                        onClick = {
                            onMoveDown()
                            onDismiss()
                        },
                        enabled = canMoveDown,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSurfaceElevated,
                            contentColor = EmeraldPrimary,
                            disabledContainerColor = EmeraldCardBorder.copy(alpha = 0.5f),
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Move Down")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done", color = TextPrimary)
                    }
                }
            }
        }
    }
}

