package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val household by viewModel.household.collectAsState()
    val monthYear by viewModel.selectedMonthYear.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val swipeableSummary by viewModel.swipeableSpendingSummary.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showAddShoppingDialog by remember { mutableStateOf(false) }
    var showHouseholdDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = EmeraldBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Top App Bar matching Screenshot 1: "Tarik ⌄", Search, Wallet/Card, Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Household name selector "Tarik ⌄"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable { showHouseholdDialog = true }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("household_dropdown_button")
                    ) {
                        Text(
                            text = household.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 22.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.UnfoldMore,
                            contentDescription = "Switch household",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Action Icons: Search, Budget/Wallet, Reports
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showSearch = !showSearch },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("search_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search expenses",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Wallet / Card icon (navigates to Budget & Income screen where fixed expenses live!)
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.BUDGET) },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("budget_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = "Go to Budget",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Statistics / Reports icon
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.STATISTICS) },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("settings_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = "Reports & Trends",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Search Bar if expanded
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search water bottle, fast food, supermarket...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = EmeraldPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = EmeraldCardBorder,
                            focusedPlaceholderColor = TextSecondary,
                            unfocusedPlaceholderColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("home_search_input")
                    )
                }

                // Month Selector: < October 2026 >
                MonthSelector(
                    monthYear = monthYear,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onResetCurrentMonth = { viewModel.resetToCurrentMonth() }
                )
            }
        },
        floatingActionButton = {
            // Emerald Floating Action Button (+) matching Screenshot 1
            FloatingActionButton(
                onClick = {
                    showAddShoppingDialog = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color(0xFF022018),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 12.dp)
                    .size(58.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add separate expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Swipeable Summary Card (Month, Week, Today with diff vs previous period)
            item {
                SwipeableHomeSpendingCard(
                    summary = swipeableSummary,
                    currency = household.currency,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Section Header: Separate Shopping & Daily Purchases List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Separate Purchases",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "${expenses.size} items",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Expenses List or Empty State (Only separate purchases appear here!)
            if (expenses.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = EmeraldSurface,
                        border = BorderStroke(1.dp, EmeraldCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B382F)),
                                contentAlignment = Alignment.Center
                            ) {
                                @Suppress("DEPRECATION")
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = EmeraldPrimaryLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "No separate expenses logged yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Log things you buy separately like fast food outside, water bottles, and supermarket groceries using the '+' button below.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseItemRow(
                        expense = expense,
                        currency = household.currency,
                        onItemClick = { expenseToEdit = expense },
                        onEditClick = { expenseToEdit = expense },
                        onDeleteClick = { expenseToDelete = expense }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Edit Shopping Expense Dialog (within 10-minute window)
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

    // Add Shopping Expense Dialog for Home Screen (things bought separately, no category selection)
    if (showAddShoppingDialog) {
        AddShoppingExpenseDialog(
            currency = household.currency,
            onDismiss = { showAddShoppingDialog = false },
            onSave = { title, amount, note ->
                viewModel.addShoppingExpense(title, amount, note)
                showAddShoppingDialog = false
            }
        )
    }

    // Household profile dialog
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
}
