package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.data.model.ExpenseEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val household by viewModel.household.collectAsState()
    val monthYear by viewModel.selectedMonthYear.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val swipeableSummary by viewModel.swipeableSpendingSummary.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val groupedExpenses by viewModel.groupedExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val backupRestoreMessage by viewModel.backupRestoreMessage.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showAddShoppingDialog by remember { mutableStateOf(false) }
    var showHouseholdDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Android Storage Access Framework (SAF) Launchers for local backup & restore
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportDataToUri(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importDataFromUri(context, uri)
        }
    }

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
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Household name selector with app header logo (weighted so long names truncate gracefully)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable { showHouseholdDialog = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .testTag("household_dropdown_button")
                    ) {
                        // Header logo: small version of the wallet-in-circle
                        IncomeControlHeaderLogo(size = 34.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = household.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 19.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Rounded.UnfoldMore,
                                contentDescription = "Switch household",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Action Icons: Search, Budget/Wallet, Reports, Settings (never disappear)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(
                            onClick = { showSearch = !showSearch },
                            modifier = Modifier
                                .size(40.dp)
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
                                .size(40.dp)
                                .testTag("budget_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = strings.budget,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Statistics / Reports icon
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.STATISTICS) },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("analytics_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = strings.statistics,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Settings / Paramètres (Light/Dark mode, French language, Light Blue & White)
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("settings_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = strings.settingsTitle,
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
                        placeholder = { Text(strings.searchPlaceholder) },
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
                contentColor = Color.White,
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
                        text = strings.separatePurchases,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "${expenses.size} ${strings.items}",
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
                                    .background(EmeraldSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                @Suppress("DEPRECATION")
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = AccentOnSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = strings.noPurchasesYet,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = strings.noPurchasesSubtext,
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
                groupedExpenses.forEach { group ->
                    val displayDayTitle = when (group.dayTitle) {
                        "Today" -> strings.today
                        "Yesterday" -> strings.yesterday
                        else -> group.dayTitle
                    }
                    stickyHeader(key = "header_${group.dayKey}") {
                        Surface(
                            color = EmeraldBackground.copy(alpha = 0.96f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayDayTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = EmeraldCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", group.dayTotal)} ${household.currency}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }

                    items(group.expenses, key = { it.id }) { expense ->
                        val isEditable = AppConstants.isExpenseEditable(expense.dateTimestamp)
                        ExpenseItemRow(
                            expense = expense,
                            currency = household.currency,
                            onItemClick = {
                                if (isEditable) {
                                    expenseToEdit = expense
                                }
                            },
                            onEditClick = {
                                if (isEditable) {
                                    expenseToEdit = expense
                                }
                            },
                            onDeleteClick = {
                                if (isEditable) {
                                    expenseToDelete = expense
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Edit Shopping Expense Dialog (within 24-hour window)
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
            onSave = { title, amount, note, timestamp ->
                viewModel.addShoppingExpense(title, amount, note, timestamp)
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
            },
            onOpenBackupRestore = {
                showBackupRestoreDialog = true
            }
        )
    }

    // Local Backup & Restore Dialog (Storage Access Framework)
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            onDismiss = { showBackupRestoreDialog = false },
            onExportBackup = {
                val filename = "income_control_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                exportLauncher.launch(filename)
            },
            onImportBackup = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            statusMessage = backupRestoreMessage,
            onClearStatus = { viewModel.clearBackupRestoreMessage() }
        )
    }
}
