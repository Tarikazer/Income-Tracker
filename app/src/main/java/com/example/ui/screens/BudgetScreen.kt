package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val household by viewModel.household.collectAsState()
    val monthYear by viewModel.selectedMonthYear.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val incomes by viewModel.monthlyIncomes.collectAsState()
    val categoryProgressList by viewModel.categoryProgressList.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val currentMonthExpenses by viewModel.currentMonthExpenses.collectAsState()

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var categoryForAddExpense by remember { mutableStateOf<CategoryEntity?>(null) }
    var showHouseholdDialog by remember { mutableStateOf(false) }

    var categoryForHistory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToReorder by remember { mutableStateOf<CategoryEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("budget_screen"),
        containerColor = EmeraldBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Top App Bar matching Screenshot 2: Back arrow, "Tarik ⌄", Chart icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.HOME) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("budget_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back to home",
                                tint = TextPrimary
                            )
                        }

                        // Household name with header logo (weighted so it truncates on long names)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable { showHouseholdDialog = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("budget_household_title")
                        ) {
                            IncomeControlHeaderLogo(size = 32.dp)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = household.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 20.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Rounded.UnfoldMore,
                                    contentDescription = "Household options",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Chart / Trends icon at top right
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.STATISTICS) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("budget_trends_icon")
                    ) {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Rounded.ShowChart,
                            contentDescription = "Interactive charts & reports",
                            tint = TextPrimary
                        )
                    }
                }

                // Month selector: < October 2026 >
                MonthSelector(
                    monthYear = monthYear,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onResetCurrentMonth = { viewModel.resetToCurrentMonth() }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Budget Summary Card (Matching Screenshot 2 exactly)
            item {
                BudgetSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalSpent = summary.totalSpent,
                    actualRemaining = summary.actualRemaining,
                    currency = household.currency,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Monthly income Section Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.monthlyIncomeSection,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    // "+ Add income" button card matching screenshot 2
                    Surface(
                        onClick = { showAddIncomeDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldSurfaceElevated,
                        border = BorderStroke(1.dp, EmeraldCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_income_card_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.addIncome,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Existing incomes list: first is Monthly Income, subsequent are Other Incomes
                    incomes.forEachIndexed { index, income ->
                        val displaySource = if (income.source.equals("Monthly income", ignoreCase = true) ||
                            income.source.equals("Monthly Income", ignoreCase = true) ||
                            income.source.equals("Other Incomes", ignoreCase = true) ||
                            income.source.equals("Other Income", ignoreCase = true) ||
                            income.source.equals("Revenu Mensuel", ignoreCase = true) ||
                            income.source.equals("Autres Revenus", ignoreCase = true)
                        ) {
                            if (index == 0) strings.monthlyIncome else strings.otherIncomes
                        } else {
                            income.source
                        }
                        IncomeRow(
                            income = income.copy(source = displaySource),
                            currency = household.currency,
                            onDelete = { viewModel.deleteIncome(income) }
                        )
                    }
                }
            }

            // Categories Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.categories,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    TextButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.testTag("add_custom_category_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.newCategory,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Category Cards matching screenshot 2:
            // Rent, Sport, Alimentation, Water Bill, Electricity Bill, Internet Bill, Family
            items(categoryProgressList, key = { it.category.id }) { catData ->
                CategoryCard(
                    categoryData = catData,
                    currency = household.currency,
                    onAddExpense = {
                        categoryForAddExpense = catData.category
                    },
                    onViewHistory = {
                        categoryForHistory = catData.category
                    },
                    onReorder = {
                        categoryToReorder = catData.category
                    },
                    onDeleteCategory = {
                        categoryToDelete = catData.category
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Dialogs:
    // Add income matching Screenshot 1 exactly (just MAD amount)
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

    // Add category expense matching Screenshot 2 exactly (MAD amount + Note optional + Helper text)
    if (categoryForAddExpense != null) {
        val cat = categoryForAddExpense!!
        AddCategoryExpenseDialog(
            categoryName = cat.name,
            currency = household.currency,
            onDismiss = { categoryForAddExpense = null },
            onSave = { amount, note ->
                viewModel.addCategoryExpense(cat, amount, note)
                categoryForAddExpense = null
            }
        )
    }

    if (categoryForHistory != null) {
        val cat = categoryForHistory!!
        val catExpenses = currentMonthExpenses.filter { it.categoryId == cat.id }
        CategoryHistoryDialog(
            category = cat,
            expenses = catExpenses,
            currency = household.currency,
            onDismiss = { categoryForHistory = null },
            onEditExpense = { expenseToEdit = it },
            onDeleteExpense = { expenseToDelete = it }
        )
    }

    // Edit Transaction Dialog for category transactions
    if (expenseToEdit != null) {
        val exp = expenseToEdit!!
        EditShoppingExpenseDialog(
            expense = exp,
            currency = household.currency,
            onDismiss = { expenseToEdit = null },
            onSave = { newTitle, newAmount, newNote ->
                viewModel.updateShoppingExpense(exp, newTitle, newAmount, newNote)
                expenseToEdit = null
            }
        )
    }

    // Delete Expense Confirmation Dialog
    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = {
                Text(
                    text = "Delete Expense?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = "Delete '${exp.title}' (${String.format(Locale.US, "%,.2f", exp.amount)} ${household.currency})?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = EmeraldSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showHouseholdDialog) {
        HouseholdDialog(
            currentName = household.name,
            currentCurrency = household.currency,
            onDismiss = { showHouseholdDialog = false },
            onSave = { name, curr ->
                viewModel.updateHousehold(name, curr)
                showHouseholdDialog = false
            }
        )
    }

    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        var expenseCount by remember(cat.id) { mutableStateOf<Int?>(null) }
        LaunchedEffect(cat.id) {
            expenseCount = viewModel.getExpenseCountForCategory(cat.id)
        }

        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = {
                Text(
                    text = strings.deleteCategoryTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = strings.deleteCategoryConfirmation(cat.name, expenseCount ?: 0),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.delete, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = EmeraldSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (categoryToReorder != null) {
        val cat = categoryToReorder!!
        val index = categoryProgressList.indexOfFirst { it.category.id == cat.id }
        ReorderCategoryDialog(
            category = cat,
            canMoveUp = index > 0,
            canMoveDown = index in 0 until categoryProgressList.size - 1,
            onMoveUp = { viewModel.moveCategoryUp(cat) },
            onMoveDown = { viewModel.moveCategoryDown(cat) },
            onDismiss = { categoryToReorder = null }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            currency = household.currency,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, iconKey ->
                viewModel.addNewCategory(name, iconKey)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun IncomeRow(
    income: IncomeEntity,
    currency: String,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("income_row_${income.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                @Suppress("DEPRECATION")
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = EmeraldPrimaryLight,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = income.source,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (income.isRecurring) {
                        Text(
                            text = "Recurring Monthly",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "+ ${String.format(Locale.US, "%,.2f", income.amount)} $currency",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = EmeraldPrimaryLight,
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete income",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("other") }

    val iconOptions = listOf(
        Pair("Shopping", "alimentation"),
        Pair("Fitness", "sport"),
        Pair("Transport", "transport"),
        Pair("Coffee", "coffee"),
        Pair("Restaurant", "restaurant"),
        Pair("Health", "health"),
        Pair("General", "other")
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = EmeraldSurface,
            border = BorderStroke(1.dp, EmeraldCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "New Category",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = EmeraldCardBorder,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { (label, key) ->
                        val isSelected = selectedIconKey == key
                        Surface(
                            onClick = { selectedIconKey = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldPrimary else EmeraldSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else EmeraldCardBorder),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = IconHelper.getCategoryIcon(key),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), selectedIconKey)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Create Category",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF032218),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
