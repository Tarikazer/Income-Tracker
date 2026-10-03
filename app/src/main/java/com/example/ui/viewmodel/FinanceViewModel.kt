package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    HOME,
    BUDGET,
    STATISTICS
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
    private val repository: FinanceRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
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
        allCategories
    ) { expenses, categories ->
        val total = expenses.sumOf { it.amount }
        val categoryMap = categories.associateBy { it.id }
        val grouped = expenses.groupBy { it.categoryId }

        grouped.mapNotNull { (catId, catExpenses) ->
            val cat = categoryMap[catId] ?: return@mapNotNull null
            val sum = catExpenses.sumOf { it.amount }
            val pct = if (total > 0) ((sum / total) * 100).toFloat() else 0f
            CategoryExpenseBreakdown(
                category = cat,
                totalAmount = sum,
                percentage = pct,
                count = catExpenses.size
            )
        }.sortedByDescending { it.totalAmount }
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
        }
    }

    fun quickAddExpense(title: String, amount: Double, categoryName: String) {
        viewModelScope.launch {
            val categories = allCategories.value
            val category = categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
                ?: categories.firstOrNull { it.iconKey == "alimentation" }
                ?: categories.firstOrNull()
                ?: return@launch

            repository.addExpense(
                ExpenseEntity(
                    title = title,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    dateTimestamp = System.currentTimeMillis(),
                    monthYear = _selectedMonthYear.value,
                    note = "Quick logged item",
                    isRecurring = false
                )
            )
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

            repository.addExpense(
                ExpenseEntity(
                    title = if (note.isNotBlank()) "${category.name} - $note" else category.name,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    dateTimestamp = System.currentTimeMillis(),
                    monthYear = _selectedMonthYear.value,
                    note = note.trim(),
                    isRecurring = isFixedCategory
                )
            )
        }
    }

    fun addIncome(amount: Double, source: String = "Monthly income", isRecurring: Boolean = true) {
        viewModelScope.launch {
            repository.addIncome(
                IncomeEntity(
                    source = source.trim(),
                    amount = amount,
                    monthYear = _selectedMonthYear.value,
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun addIncomeWithSource(source: String, amount: Double, isRecurring: Boolean = true) {
        viewModelScope.launch {
            repository.addIncome(
                IncomeEntity(
                    source = source.trim(),
                    amount = amount,
                    monthYear = _selectedMonthYear.value,
                    isRecurring = isRecurring
                )
            )
        }
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
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
