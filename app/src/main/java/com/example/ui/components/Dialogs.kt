package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.AccentRed
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.LocalAppStrings
import com.example.util.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddIncomeDialog(
    currency: String,
    existingIncomesCount: Int = 0,
    onDismiss: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isFirstIncome = existingIncomesCount == 0
    val incomeCategoryLabel = if (isFirstIncome) strings.monthlyIncome else strings.otherIncomes

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("add_income_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.addIncomeTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isFirstIncome) strings.firstIncomeHelperText else strings.otherIncomeHelperText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("$incomeCategoryLabel ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AccentRed)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_amount_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_income_button")
                    ) {
                        Text(text = strings.cancel, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanAmount = amountText.trim().replace(',', '.').toDoubleOrNull()
                            if (cleanAmount == null || cleanAmount <= 0.0) {
                                errorMessage = strings.pleaseEnterValidAmount
                            } else {
                                onSave(cleanAmount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("save_income_button")
                    ) {
                        Text(text = strings.save, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCategoryExpenseDialog(
    categoryName: String,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String, coversMonths: Int) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCoversMonths by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("add_category_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "${strings.addExpense} · $categoryName",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${strings.addExpense} ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AccentRed)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_expense_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(strings.noteOptionalLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.coversLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val durationOptions = listOf(
                    1 to strings.month1,
                    2 to strings.months2,
                    3 to strings.months3,
                    6 to strings.months6,
                    12 to strings.months12
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durationOptions.forEach { (months, label) ->
                        val isSelected = selectedCoversMonths == months
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCoversMonths = months },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("covers_chip_$months")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_category_expense_button")
                    ) {
                        Text(text = strings.cancel, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanAmount = amountText.trim().replace(',', '.').toDoubleOrNull()
                            if (cleanAmount == null || cleanAmount <= 0.0) {
                                errorMessage = strings.pleaseEnterValidAmount
                            } else {
                                onSave(cleanAmount, noteText.trim(), selectedCoversMonths)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("save_category_expense_button")
                    ) {
                        Text(text = strings.save, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddShoppingExpenseDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, note: String, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val calendar = remember(selectedTimestamp) {
        Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("add_shopping_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.addPurchaseTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.purchaseNameLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${strings.totalSpent} ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AccentRed)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(strings.noteOptionalLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.dateTimeLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, day)
                                        selectedTimestamp = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(Date(selectedTimestamp)), fontSize = 13.sp, color = colors.textPrimary)
                        }
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        selectedTimestamp = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeFormat.format(Date(selectedTimestamp)), fontSize = 13.sp, color = colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_shopping_button")
                    ) {
                        Text(text = strings.cancel, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanAmount = amountText.trim().replace(',', '.').toDoubleOrNull()
                            if (cleanAmount == null || cleanAmount <= 0.0) {
                                errorMessage = strings.pleaseEnterValidAmount
                            } else {
                                val finalTitle = title.trim().ifEmpty { strings.separatePurchases }
                                onSave(finalTitle, cleanAmount, note.trim(), selectedTimestamp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("save_shopping_button")
                    ) {
                        Text(text = strings.save, color = Color.White)
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
    onSave: (title: String, amount: Double, note: String, coversMonths: Int) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    var title by remember { mutableStateOf(expense.title) }
    var amountText by remember { mutableStateOf(AppConstants.formatCurrency(expense.amount)) }
    var note by remember { mutableStateOf(expense.note) }
    var selectedCoversMonths by remember { mutableIntStateOf(expense.coversMonths) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateTimeFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("edit_shopping_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.editPurchaseTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.purchaseNameLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("${strings.totalSpent} ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AccentRed)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(strings.noteOptionalLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.dateTimeLabel}: ${dateTimeFormat.format(Date(expense.dateTimestamp))}",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontSize = 12.sp)
                        )
                    }
                }

                if (expense.categoryId != 0L) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.coversLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val durationOptions = listOf(
                        1 to strings.month1,
                        2 to strings.months2,
                        3 to strings.months3,
                        6 to strings.months6,
                        12 to strings.months12
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationOptions.forEach { (months, label) ->
                            FilterChip(
                                selected = selectedCoversMonths == months,
                                onClick = { selectedCoversMonths = months },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = strings.cancel, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanAmount = amountText.trim().replace(',', '.').toDoubleOrNull()
                            if (cleanAmount == null || cleanAmount <= 0.0) {
                                errorMessage = strings.pleaseEnterValidAmount
                            } else {
                                onSave(title.trim().ifEmpty { expense.title }, cleanAmount, note.trim(), selectedCoversMonths)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = strings.update, color = Color.White)
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
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(currentName) }
    var currency by remember { mutableStateOf(currentCurrency) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("household_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.householdSection,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.householdName) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text(strings.currencyLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("household_currency_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_household_button")
                    ) {
                        Text(text = strings.cancel, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name.trim().ifEmpty { "Tarik" }, currency.trim().ifEmpty { "MAD" })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("save_household_button")
                    ) {
                        Text(text = strings.save, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    statusMessage: String? = null,
    onClearStatus: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.backupRestore,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strings.backupDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup_button")
                ) {
                    Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.exportBackup)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_backup_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.restoreBackup)
                }

                if (!statusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = strings.close, color = Color.White)
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
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("reorder_category_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "${strings.categories} · ${category.name}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Up")
                    }

                    OutlinedButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Down")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = strings.done, color = Color.White)
                    }
                }
            }
        }
    }
}
