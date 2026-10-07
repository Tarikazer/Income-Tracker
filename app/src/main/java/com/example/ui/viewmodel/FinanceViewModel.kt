package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.ui.util.AppLanguage
import com.example.ui.util.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    HOME,
    BUDGET,
    STATISTICS,
    SETTINGS
}

data class CategoryExpenseBreakdown(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class DailySpendPoint(
    val dayOfMonth: Int,
    val dateLabel: String,
    val amount: Double
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val context: Context? = null
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    // Theme Mode (Dark, Light, System) - persisted in SharedPreferences (default Light Blue & White)
    private val _themeMode = MutableStateFlow(context?.let { loadPersistedThemeMode(it) } ?: ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Language (English, French) - persisted in SharedPreferences
    private val _language = MutableStateFlow(context?.let { loadPersistedLanguage(it) } ?: AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setThemeMode(mode: ThemeMode, appContext: Context? = null) {
        _themeMode.value = mode
        val ctx = appContext ?: context
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences("income_control_settings", Context.MODE_PRIVATE)
            prefs.edit().putString("theme_mode", mode.name).apply()
        }
    }

    fun setLanguage(lang: AppLanguage, appContext: Context? = null) {
        _language.value = lang
        val ctx = appContext ?: context
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences("income_control_settings", Context.MODE_PRIVATE)
            prefs.edit().putString("app_language", lang.name).apply()
        }
    }

    private fun loadPersistedThemeMode(ctx: Context): ThemeMode {
        return try {
            val prefs = ctx.getSharedPreferences("income_control_settings", Context.MODE_PRIVATE)
            val name = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }

    private fun loadPersistedLanguage(ctx: Context): AppLanguage {
        return try {
            val prefs = ctx.getSharedPreferences("income_control_settings", Context.MODE_PRIVATE)
            val name = prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    // Current selected month: defaults to system time "2026-10"
    private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYear())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    // Navigation screen state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Home screen filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryFilter: StateFlow<Long?> = _selectedCategoryFilter.asStateFlow()

    // Backup & Restore operation status feedback
    private val _backupRestoreMessage = MutableStateFlow<String?>(null)
    val backupRestoreMessage: StateFlow<String?> = _backupRestoreMessage.asStateFlow()

    // Active household
    val household: StateFlow<HouseholdEntity> = repository.primaryHousehold
        .map { it ?: HouseholdEntity(name = "Tarik", currency = "MAD") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HouseholdEntity(name = "Tarik", currency = "MAD")
        )

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySummary: StateFlow<MonthlyFinanceSummary> = _selectedMonthYear
        .flatMapLatest { month -> repository.getMonthlySummary(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlyFinanceSummary(
                monthYear = getCurrentMonthYear(),
                totalIncome = 0.0,
                totalSpent = 0.0,
                shoppingSpent = 0.0,
                actualRemaining = 0.0
            )
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val swipeableSpendingSummary: StateFlow<SwipeableSpendingSummary> = _selectedMonthYear
        .flatMapLatest { month -> repository.getSwipeableSpendingSummary(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SwipeableSpendingSummary(
                monthSpending = PeriodSpending("Total spent this month", 0.0, 0.0, 0.0, "than last month"),
                weekSpending = PeriodSpending("Total spent this week", 0.0, 0.0, 0.0, "than last week"),
                todaySpending = PeriodSpending("Total spent today", 0.0, 0.0, 0.0, "than yesterday")
            )
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyIncomes: StateFlow<List<IncomeEntity>> = _selectedMonthYear
        .flatMapLatest { month -> repository.getIncomesForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryProgressList: StateFlow<List<CategoryWithSpent>> = _selectedMonthYear
        .flatMapLatest { month -> repository.getCategorySpendingForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthExpenses: StateFlow<List<ExpenseEntity>> = _selectedMonthYear
        .flatMapLatest { month -> repository.getExpensesForMonth(month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered separate shopping expenses for Home Screen (fixed expenses like rent and sport are hidden)
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        currentMonthExpenses,
        _searchQuery,
        _selectedCategoryFilter
    ) { expenses, query, catId ->
        expenses.filter { expense ->
            val isSeparateShopping = !expense.isRecurring
            val matchesQuery = query.isBlank() ||
                    expense.title.contains(query, ignoreCase = true) ||
                    expense.categoryName.contains(query, ignoreCase = true) ||
                    expense.note.contains(query, ignoreCase = true)
            val matchesCategory = catId == null || expense.categoryId == catId
            isSeparateShopping && matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Grouped purchases by day for sticky day headers
    val groupedExpenses: StateFlow<List<DailyExpenseGroup>> = filteredExpenses
        .map { expenses -> groupExpensesByDay(expenses) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Statistics computations
    val categoryBreakdown: StateFlow<List<CategoryExpenseBreakdown>> = combine(
        currentMonthExpenses,
        allCategories,
        language
    ) { expenses, categories, lang ->
        val total = expenses.sumOf { it.amount }
        val categoryMap = categories.associateBy { it.id }

        val knownCategoryExpenses = mutableMapOf<CategoryEntity, MutableList<ExpenseEntity>>()
        val separatePurchases = mutableListOf<ExpenseEntity>()

        for (expense in expenses) {
            val cat = categoryMap[expense.categoryId]
            if (cat != null) {
                knownCategoryExpenses.getOrPut(cat) { mutableListOf() }.add(expense)
            } else {
                separatePurchases.add(expense)
            }
        }

        val list = mutableListOf<CategoryExpenseBreakdown>()
        for ((cat, catExpenses) in knownCategoryExpenses) {
            val sum = catExpenses.sumOf { it.amount }
            val pct = if (total > 0) ((sum / total) * 100).toFloat() else 0f
            list.add(
                CategoryExpenseBreakdown(
                    category = cat,
                    totalAmount = sum,
                    percentage = pct,
                    count = catExpenses.size
                )
            )
        }

        if (separatePurchases.isNotEmpty()) {
            val separateName = if (lang == AppLanguage.FRENCH) "Achats séparés" else "Separate Purchases"
            val separateCategory = CategoryEntity(
                id = 0L,
                name = separateName,
                iconKey = "shopping",
                isRecurring = false,
                colorHex = 0xFF64748BL, // neutral slate color
                householdId = 1,
                displayOrder = 999
            )
            val sum = separatePurchases.sumOf { it.amount }
            val pct = if (total > 0) ((sum / total) * 100).toFloat() else 0f
            list.add(
                CategoryExpenseBreakdown(
                    category = separateCategory,
                    totalAmount = sum,
                    percentage = pct,
                    count = separatePurchases.size
                )
            )
        }

        list.sortedByDescending { it.totalAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailySpendingTrend: StateFlow<List<DailySpendPoint>> = combine(
        currentMonthExpenses,
        _selectedMonthYear
    ) { expenses, monthYear ->
        val cal = Calendar.getInstance()
        val parts = monthYear.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val monthIndex = (parts.getOrNull(1)?.toIntOrNull() ?: 10) - 1
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dayExpenseMap = mutableMapOf<Int, Double>()
        for (i in 1..maxDays) {
            dayExpenseMap[i] = 0.0
        }

        val calItem = Calendar.getInstance()
        expenses.forEach { exp ->
            calItem.timeInMillis = exp.dateTimestamp
            val expMonth = calItem.get(Calendar.MONTH)
            val expYear = calItem.get(Calendar.YEAR)
            if (expMonth == monthIndex && expYear == year) {
                val day = calItem.get(Calendar.DAY_OF_MONTH)
                dayExpenseMap[day] = (dayExpenseMap[day] ?: 0.0) + exp.amount
            }
        }

        dayExpenseMap.entries.sortedBy { it.key }.map { (day, amount) ->
            DailySpendPoint(
                dayOfMonth = day,
                dateLabel = "$day",
                amount = amount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation methods
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        return if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            true
        } else {
            false
        }
    }

    // Month navigation
    fun nextMonth() {
        _selectedMonthYear.value = changeMonth(_selectedMonthYear.value, 1)
    }

    fun previousMonth() {
        _selectedMonthYear.value = changeMonth(_selectedMonthYear.value, -1)
    }

    fun resetToCurrentMonth() {
        _selectedMonthYear.value = getCurrentMonthYear()
    }

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == categoryId) null else categoryId
    }

    // Data mutations
    fun addExpense(
        title: String,
        amount: Double,
        category: CategoryEntity,
        note: String = "",
        isRecurring: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val expenseMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timestamp))
            repository.addExpense(
                ExpenseEntity(
                    title = title.trim(),
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    dateTimestamp = timestamp,
                    monthYear = expenseMonthYear,
                    note = note.trim(),
                    isRecurring = isRecurring
                )
            )
            if (_selectedMonthYear.value != expenseMonthYear) {
                _selectedMonthYear.value = expenseMonthYear
            }
        }
    }

    fun quickAddExpense(title: String, amount: Double, categoryName: String) {
        viewModelScope.launch {
            val categories = allCategories.value
            val category = categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
                ?: categories.firstOrNull { it.iconKey == "alimentation" }
                ?: categories.firstOrNull()
                ?: return@launch

            val now = System.currentTimeMillis()
            val expenseMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
            repository.addExpense(
                ExpenseEntity(
                    title = title,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    dateTimestamp = now,
                    monthYear = expenseMonthYear,
                    note = "Quick logged item",
                    isRecurring = false
                )
            )
            if (_selectedMonthYear.value != expenseMonthYear) {
                _selectedMonthYear.value = expenseMonthYear
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addShoppingExpense(
        title: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val expenseMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timestamp))
            repository.addExpense(
                ExpenseEntity(
                    title = title.trim(),
                    amount = amount,
                    categoryId = 0L,
                    categoryName = "Separate Purchase",
                    categoryIconKey = "shopping",
                    dateTimestamp = timestamp,
                    monthYear = expenseMonthYear,
                    note = note.trim(),
                    isRecurring = false // Separate purchase on Home Screen, never linked to Alimentation
                )
            )
            if (_selectedMonthYear.value != expenseMonthYear) {
                _selectedMonthYear.value = expenseMonthYear
            }
        }
    }

    fun addCategoryExpense(
        category: CategoryEntity,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            // Categories like Rent, Sport, Bills, Family are fixed expenses and stay in wallet section
            val isFixedCategory = category.name.contains("Rent", ignoreCase = true) ||
                    category.name.contains("Sport", ignoreCase = true) ||
                    category.name.contains("Bill", ignoreCase = true) ||
                    category.name.contains("Family", ignoreCase = true) ||
                    category.isRecurring

            val now = System.currentTimeMillis()
            val expenseMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
            repository.addExpense(
                ExpenseEntity(
                    title = if (note.isNotBlank()) "${category.name} - $note" else category.name,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    dateTimestamp = now,
                    monthYear = expenseMonthYear,
                    note = note.trim(),
                    isRecurring = isFixedCategory
                )
            )
            if (_selectedMonthYear.value != expenseMonthYear) {
                _selectedMonthYear.value = expenseMonthYear
            }
        }
    }

    fun addIncome(amount: Double, source: String? = null, isRecurring: Boolean = true) {
        viewModelScope.launch {
            val currentIncomes = monthlyIncomes.value
            val label = if (!source.isNullOrBlank() &&
                !source.equals("Monthly income", ignoreCase = true) &&
                !source.equals("Monthly Income", ignoreCase = true) &&
                !source.equals("Other Incomes", ignoreCase = true) &&
                !source.equals("Other Income", ignoreCase = true)
            ) {
                source.trim()
            } else {
                if (currentIncomes.isEmpty()) "Monthly Income" else "Other Incomes"
            }
            val now = System.currentTimeMillis()
            val incomeMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
            repository.addIncome(
                IncomeEntity(
                    source = label,
                    amount = amount,
                    monthYear = incomeMonthYear,
                    isRecurring = isRecurring,
                    dateTimestamp = now
                )
            )
            if (_selectedMonthYear.value != incomeMonthYear) {
                _selectedMonthYear.value = incomeMonthYear
            }
        }
    }

    fun addIncomeWithSource(source: String, amount: Double, isRecurring: Boolean = true) {
        viewModelScope.launch {
            val currentIncomes = monthlyIncomes.value
            val label = if (source.isBlank() ||
                source.equals("Monthly income", ignoreCase = true) ||
                source.equals("Monthly Income", ignoreCase = true) ||
                source.equals("Other Incomes", ignoreCase = true) ||
                source.equals("Other Income", ignoreCase = true)
            ) {
                if (currentIncomes.isEmpty()) "Monthly Income" else "Other Incomes"
            } else {
                source.trim()
            }
            val now = System.currentTimeMillis()
            val incomeMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
            repository.addIncome(
                IncomeEntity(
                    source = label,
                    amount = amount,
                    monthYear = incomeMonthYear,
                    isRecurring = isRecurring,
                    dateTimestamp = now
                )
            )
            if (_selectedMonthYear.value != incomeMonthYear) {
                _selectedMonthYear.value = incomeMonthYear
            }
        }
    }

    suspend fun getExpenseCountForCategory(categoryId: Long): Int {
        return repository.getExpenseCountForCategory(categoryId)
    }

    // Local Backup & Restore using Android Storage Access Framework
    fun exportDataToUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = repository.exportBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
                _backupRestoreMessage.value = "Backup successfully exported to device."
            } catch (e: Exception) {
                _backupRestoreMessage.value = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun importDataFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalArgumentException("Could not read backup file")

                val result = repository.importBackupJson(json)
                if (result.isSuccess) {
                    _backupRestoreMessage.value = "Data restored successfully! ${result.getOrNull() ?: ""}"
                } else {
                    _backupRestoreMessage.value = "Restore failed: ${result.exceptionOrNull()?.localizedMessage ?: "Invalid file"}"
                }
            } catch (e: Exception) {
                _backupRestoreMessage.value = "Restore error: ${e.localizedMessage ?: "Invalid file"}"
            }
        }
    }

    fun clearBackupRestoreMessage() {
        _backupRestoreMessage.value = null
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun moveCategoryUp(category: CategoryEntity) {
        viewModelScope.launch {
            repository.moveCategoryUp(category)
        }
    }

    fun moveCategoryDown(category: CategoryEntity) {
        viewModelScope.launch {
            repository.moveCategoryDown(category)
        }
    }

    fun updateShoppingExpense(expense: ExpenseEntity, newTitle: String, newAmount: Double, newNote: String) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    title = newTitle.trim(),
                    amount = newAmount,
                    note = newNote.trim()
                )
            )
        }
    }

    fun updateHousehold(name: String, currency: String = "MAD") {
        viewModelScope.launch {
            val currentH = household.value
            repository.updateHouseholdName(currentH.id, name.trim(), currency.trim())
        }
    }

    fun addNewCategory(name: String, iconKey: String, isRecurring: Boolean = true) {
        viewModelScope.launch {
            repository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    iconKey = iconKey,
                    isRecurring = isRecurring
                )
            )
        }
    }

    companion object {
        fun groupExpensesByDay(expenses: List<ExpenseEntity>): List<DailyExpenseGroup> {
            val sorted = expenses.sortedByDescending { it.dateTimestamp }
            val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

            val todayKey = dayFormat.format(Date())
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayKey = dayFormat.format(cal.time)

            return sorted.groupBy { dayFormat.format(Date(it.dateTimestamp)) }
                .map { (key, dayExpenses) ->
                    val title = when (key) {
                        todayKey -> "Today"
                        yesterdayKey -> "Yesterday"
                        else -> {
                            val firstDate = Date(dayExpenses.first().dateTimestamp)
                            displayFormat.format(firstDate)
                        }
                    }
                    val total = dayExpenses.sumOf { it.amount }
                    DailyExpenseGroup(
                        dayKey = key,
                        dayTitle = title,
                        dayTotal = total,
                        expenses = dayExpenses
                    )
                }
        }

        fun getCurrentMonthYear(): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            return sdf.format(Date())
        }

        fun formatMonthYearDisplay(monthYear: String): String {
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM", Locale.US)
                val date = inputSdf.parse(monthYear) ?: return monthYear
                val outputSdf = SimpleDateFormat("MMMM yyyy", Locale.US)
                outputSdf.format(date)
            } catch (e: Exception) {
                monthYear
            }
        }

        private fun changeMonth(current: String, delta: Int): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                val date = sdf.parse(current) ?: Date()
                val cal = Calendar.getInstance()
                cal.time = date
                cal.add(Calendar.MONTH, delta)
                sdf.format(cal.time)
            } catch (e: Exception) {
                current
            }
        }
    }
}

class FinanceViewModelFactory(
    private val repository: FinanceRepository,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            return FinanceViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
