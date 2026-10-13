package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeEntity
import com.example.ui.components.*
import com.example.ui.theme.AccentRed
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppConstants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current

    val household by viewModel.household.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val incomes by viewModel.incomesForMonth.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val categoryProgressList by viewModel.categoryProgressList.collectAsState()
    val categoryCoverageMap by viewModel.categoryCoverageMap.collectAsState()
    val allExpenses by viewModel.currentMonthExpenses.collectAsState()

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryForAddExpense by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryForHistory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToReorder by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var incomeToDelete by remember { mutableStateOf<IncomeEntity?>(null) }
    var coveredCategoryToAddAnyway by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.budgetNav,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 20.sp
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.testTag("add_category_app_bar_button")
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = strings.newCategory, tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize().testTag("budget_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthSelector(
                    monthYear = selectedMonth,
                    onPreviousMonth = { viewModel.selectPreviousMonth() },
                    onNextMonth = { viewModel.selectNextMonth() },
                    onResetCurrentMonth = { viewModel.selectCurrentMonth() }
                )
            }

            // Summary banner (Total Income / Spent / Balance)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("budget_summary_banner")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.monthlyFinanceSummary,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${AppConstants.formatCurrency(monthlySummary.actualRemaining)} ${household.currency}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthlySummary.actualRemaining >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(strings.totalIncome, style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontSize = 12.sp))
                                Text(
                                    "${AppConstants.formatCurrency(monthlySummary.totalIncome)} ${household.currency}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(strings.totalSpent, style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontSize = 12.sp))
                                Text(
                                    "${AppConstants.formatCurrency(monthlySummary.totalSpent)} ${household.currency}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                )
                            }
                        }
                    }
                }
            }

            // Income section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.monthlyIncomeSection,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 17.sp
                        )
                    )
                    TextButton(
                        onClick = { showAddIncomeDialog = true },
                        modifier = Modifier.testTag("add_income_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.addIncome, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            if (incomes.isEmpty()) {
                item(key = "empty_incomes") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.noIncomeYet,
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(incomes, key = { "income_${it.id}" }) { income ->
                    IncomeRow(
                        income = income,
                        currency = household.currency,
                        onDelete = { incomeToDelete = income }
                    )
                }
            }

            // Categories Section
            item(key = "categories_section_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.categories,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 17.sp
                        )
                    )
                    FilledTonalButton(
                        onClick = { showAddCategoryDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_category_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.newCategory, fontSize = 13.sp)
                    }
                }
            }

            if (categoryProgressList.isEmpty()) {
                item(key = "empty_categories") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.noCategoriesYet,
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(categoryProgressList, key = { "category_${it.category.id}" }) { item ->
                    val coverage = categoryCoverageMap[item.category.id]
                    CategoryCard(
                        categoryData = item,
                        currency = household.currency,
                        coverage = coverage,
                        onAddExpense = {
                            if (coverage != null) {
                                coveredCategoryToAddAnyway = item.category
                            } else {
                                categoryForAddExpense = item.category
                            }
                        },
                        onViewHistory = { categoryForHistory = item.category },
                        onReorder = { categoryToReorder = item.category },
                        onDeleteCategory = { categoryToDelete = item.category }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Income Dialog
    if (showAddIncomeDialog) {
        AddIncomeDialog(
            currency = household.currency,
            existingIncomesCount = incomes.size,
            onDismiss = { showAddIncomeDialog = false },
            onSave = { amount ->
                viewModel.addIncome(amount)
                showAddIncomeDialog = false
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            currency = household.currency,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, iconKey ->
                viewModel.addCategory(name = name, iconKey = iconKey)
                showAddCategoryDialog = false
            }
        )
    }

    // Add Category Expense Dialog
    if (categoryForAddExpense != null) {
        val cat = categoryForAddExpense!!
        AddCategoryExpenseDialog(
            categoryName = cat.name,
            currency = household.currency,
            onDismiss = { categoryForAddExpense = null },
            onSave = { amount, note, coversMonths ->
                viewModel.addCategoryExpense(cat, amount, note, coversMonths)
                categoryForAddExpense = null
            }
        )
    }

    // Add Anyway confirmation if already covered
    if (coveredCategoryToAddAnyway != null) {
        val cat = coveredCategoryToAddAnyway!!
        val cov = categoryCoverageMap[cat.id]
        AlertDialog(
            onDismissRequest = { coveredCategoryToAddAnyway = null },
            title = { Text(strings.coveredAddAnywayTitle(cat.name, cov?.endMonthYear ?: "")) },
            text = { Text(strings.alreadyPaidCoveredUntil(cov?.endMonthYear ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        categoryForAddExpense = cat
                        coveredCategoryToAddAnyway = null
                    }
                ) {
                    Text(strings.addAnyway)
                }
            },
            dismissButton = {
                TextButton(onClick = { coveredCategoryToAddAnyway = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Category History Dialog
    if (categoryForHistory != null) {
        val cat = categoryForHistory!!
        val catExpenses = allExpenses.filter { it.categoryId == cat.id }
        CategoryHistoryDialog(
            category = cat,
            expenses = catExpenses,
            currency = household.currency,
            coverage = categoryCoverageMap[cat.id],
            onDismiss = { categoryForHistory = null },
            onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
        )
    }

    // Reorder Dialog
    if (categoryToReorder != null) {
        val cat = categoryToReorder!!
        val index = categoryProgressList.indexOfFirst { it.category.id == cat.id }
        ReorderCategoryDialog(
            category = cat,
            canMoveUp = index > 0,
            canMoveDown = index in 0 until (categoryProgressList.size - 1),
            onMoveUp = { viewModel.moveCategoryUp(cat) },
            onMoveDown = { viewModel.moveCategoryDown(cat) },
            onDismiss = { categoryToReorder = null }
        )
    }

    // Delete Category Confirmation Dialog
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        val item = categoryProgressList.firstOrNull { it.category.id == cat.id }
        val count = item?.expensesCount ?: 0
        ConfirmDeleteDialog(
            title = strings.deleteCategoryTitle,
            message = strings.deleteCategoryFromMonthConfirmation(cat.name, selectedMonth, count),
            confirmText = strings.delete,
            cancelText = strings.cancel,
            onConfirm = {
                viewModel.deleteCategory(cat)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }

    // Delete Income Confirmation Dialog
    if (incomeToDelete != null) {
        val inc = incomeToDelete!!
        val amountStr = String.format(Locale.US, "%,.2f", inc.amount)
        ConfirmDeleteDialog(
            title = strings.deleteIncomeTitle,
            message = strings.deleteIncomeConfirmation(inc.source, amountStr, household.currency),
            confirmText = strings.delete,
            cancelText = strings.cancel,
            onConfirm = {
                viewModel.deleteIncome(inc)
                incomeToDelete = null
            },
            onDismiss = { incomeToDelete = null }
        )
    }
}

@Composable
fun IncomeRow(
    income: IncomeEntity,
    currency: String,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth().testTag("income_row_${income.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = income.source,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 15.sp
                    )
                )
                if (income.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = income.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+${AppConstants.formatCurrency(income.amount)} $currency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_income_${income.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Delete income",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("shopping") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val iconOptions = listOf(
        "shopping" to "Shopping",
        "food" to "Food",
        "restaurant" to "Dining",
        "rent" to "Rent",
        "transport" to "Transport",
        "water" to "Water",
        "power" to "Power",
        "wifi" to "Internet",
        "coffee" to "Coffee",
        "health" to "Health",
        "sport" to "Fitness",
        "family" to "Family"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.dialogSurface,
            border = BorderStroke(1.dp, colors.dialogBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("add_category_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = strings.newCategory,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Category Name") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AccentRed)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Icon",
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
                    iconOptions.take(4).forEach { (key, _) ->
                        val isSelected = selectedIconKey == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedIconKey = key }
                                .padding(vertical = 8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Icon(
                                    imageVector = IconHelper.getCategoryIcon(key),
                                    contentDescription = key,
                                    tint = if (isSelected) colors.primary else colors.textPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                            val cleanName = name.trim()
                            if (cleanName.isEmpty()) {
                                errorMessage = "Name cannot be empty"
                            } else {
                                onSave(cleanName, selectedIconKey)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("save_category_button")
                    ) {
                        Text(text = strings.save, color = Color.White)
                    }
                }
            }
        }
    }
}
