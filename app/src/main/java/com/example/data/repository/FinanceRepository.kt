package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(
    private val householdDao: HouseholdDao,
    private val categoryDao: CategoryDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {
    val primaryHousehold: Flow<HouseholdEntity?> = householdDao.getPrimaryHousehold()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun ensureDefaultData() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(
                    id = 1,
                    name = "Rent",
                    iconKey = "rent",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFF10B981
                ),
                CategoryEntity(
                    id = 2,
                    name = "Sport",
                    iconKey = "sport",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFF06B6D4
                ),
                CategoryEntity(
                    id = 3,
                    name = "Alimentation",
                    iconKey = "alimentation",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFFF59E0B
                ),
                CategoryEntity(
                    id = 4,
                    name = "Water Bill",
                    iconKey = "water",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFF3B82F6
                ),
                CategoryEntity(
                    id = 5,
                    name = "Electricity Bill",
                    iconKey = "electricity",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFFEAB308
                ),
                CategoryEntity(
                    id = 6,
                    name = "Internet Bill",
                    iconKey = "internet",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFF8B5CF6
                ),
                CategoryEntity(
                    id = 7,
                    name = "Family",
                    iconKey = "family",
                    isRecurring = true,
                    defaultPlannedAmount = 0.0,
                    colorHex = 0xFFEC4899
                )
            )
            categoryDao.insertCategories(defaultCategories)
        }
        // Also cleanup any shopping expenses that were mistakenly saved under Alimentation
        expenseDao.detachShoppingFromAlimentation()
    }

    fun getExpensesForMonth(monthYear: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForMonth(monthYear)
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    fun getIncomesForMonth(monthYear: String): Flow<List<IncomeEntity>> {
        return incomeDao.getIncomesForMonth(monthYear)
    }

    fun getAllIncomes(): Flow<List<IncomeEntity>> {
        return incomeDao.getAllIncomes()
    }

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(monthYear)
    }

    fun getCategoryProgressForMonth(monthYear: String): Flow<List<CategoryWithBudgetAndSpent>> {
        return combine(
            categoryDao.getAllCategories(),
            budgetDao.getBudgetsForMonth(monthYear),
            expenseDao.getExpensesForMonth(monthYear)
        ) { categories, budgets, expenses ->
            val budgetMap = budgets.associateBy { it.categoryId }
            val expensesByCategory = expenses.groupBy { it.categoryId }

            categories.map { category ->
                val planned = budgetMap[category.id]?.plannedAmount ?: category.defaultPlannedAmount
                val catExpenses = expensesByCategory[category.id] ?: emptyList()
                val spent = catExpenses.sumOf { it.amount }
                CategoryWithBudgetAndSpent(
                    category = category,
                    plannedAmount = planned,
                    spentAmount = spent,
                    expensesCount = catExpenses.size
                )
            }
        }
    }

    fun getMonthlySummary(monthYear: String): Flow<MonthlyFinanceSummary> {
        return combine(
            getCategoryProgressForMonth(monthYear),
            incomeDao.getIncomesForMonth(monthYear),
            expenseDao.getExpensesForMonth(monthYear)
        ) { categoryProgress, incomes, expenses ->
            val totalIncome = incomes.sumOf { it.amount }
            val totalPlanned = categoryProgress.sumOf { it.plannedAmount }
            val totalSpent = expenses.sumOf { it.amount }
            
            // Shopping spent: day to day expenses (non-recurring or general shopping/food)
            val shoppingSpent = expenses.filter { !it.isRecurring }.sumOf { it.amount }
            
            val plannedRemaining = totalIncome - totalPlanned
            val actualRemaining = totalIncome - totalSpent

            MonthlyFinanceSummary(
                monthYear = monthYear,
                totalIncome = totalIncome,
                totalPlanned = totalPlanned,
                totalSpent = totalSpent,
                shoppingSpent = shoppingSpent,
                plannedRemaining = plannedRemaining,
                actualRemaining = actualRemaining
            )
        }
    }

    suspend fun updateHouseholdName(id: Long, name: String, currency: String) {
        householdDao.updateHousehold(HouseholdEntity(id = id, name = name, currency = currency))
    }

    suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
        budgetDao.deleteBudgetsForCategory(category.id)
        expenseDao.deleteExpensesForCategory(category.id)
    }

    suspend fun addIncome(income: IncomeEntity): Long {
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.deleteIncome(income)
    }

    suspend fun addExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun setCategoryBudget(categoryId: Long, monthYear: String, plannedAmount: Double) {
        val existing = budgetDao.getBudgetForCategory(categoryId, monthYear)
        if (existing != null) {
            budgetDao.insertBudget(existing.copy(plannedAmount = plannedAmount))
        } else {
            budgetDao.insertBudget(
                BudgetEntity(
                    categoryId = categoryId,
                    monthYear = monthYear,
                    plannedAmount = plannedAmount
                )
            )
        }
        categoryDao.updateDefaultPlannedAmount(categoryId, plannedAmount)
    }

    suspend fun moveCategoryUp(category: CategoryEntity) {
        val list = categoryDao.getCategoriesList()
        val index = list.indexOfFirst { it.id == category.id }
        if (index > 0) {
            val prev = list[index - 1]
            categoryDao.updateCategoryOrder(category.id, prev.displayOrder)
            categoryDao.updateCategoryOrder(prev.id, category.displayOrder)
            normalizeCategoryOrders()
        }
    }

    suspend fun moveCategoryDown(category: CategoryEntity) {
        val list = categoryDao.getCategoriesList()
        val index = list.indexOfFirst { it.id == category.id }
        if (index in 0 until list.size - 1) {
            val next = list[index + 1]
            categoryDao.updateCategoryOrder(category.id, next.displayOrder)
            categoryDao.updateCategoryOrder(next.id, category.displayOrder)
            normalizeCategoryOrders()
        }
    }

    private suspend fun normalizeCategoryOrders() {
        val list = categoryDao.getCategoriesList()
        list.forEachIndexed { i, cat ->
            if (cat.displayOrder != i) {
                categoryDao.updateCategoryOrder(cat.id, i)
            }
        }
    }

    suspend fun updateRentPrice(categoryId: Long, newPrice: Double, monthYear: String) {
        val now = System.currentTimeMillis()
        categoryDao.updateRentPrice(categoryId, newPrice, now)
        // Also update or insert the Rent expense for the current month
        val rentExpenses = expenseDao.getExpensesForCategory(categoryId, monthYear).firstOrNull() ?: emptyList()
        if (rentExpenses.isNotEmpty()) {
            val first = rentExpenses.first()
            expenseDao.updateExpense(first.copy(amount = newPrice, dateTimestamp = now))
        } else {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Rent",
                    amount = newPrice,
                    categoryId = categoryId,
                    categoryName = "Rent",
                    categoryIconKey = "rent",
                    dateTimestamp = now,
                    monthYear = monthYear,
                    isRecurring = true
                )
            )
        }
    }

    fun getSwipeableSpendingSummary(currentMonthYear: String): Flow<SwipeableSpendingSummary> {
        return expenseDao.getAllExpenses().map { allExpenses ->
            calculateSwipeableSpending(allExpenses, currentMonthYear)
        }
    }

    private fun calculateSwipeableSpending(
        allExpenses: List<ExpenseEntity>,
        currentMonthYear: String
    ): SwipeableSpendingSummary {
        val shopping = allExpenses.filter { !it.isRecurring }

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = now

        // 1. Today vs Yesterday
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        val endOfToday = startOfToday + 24 * 3600 * 1000L - 1
        val startOfYesterday = startOfToday - 24 * 3600 * 1000L
        val endOfYesterday = startOfToday - 1

        val todaySpent = shopping.filter { it.dateTimestamp in startOfToday..endOfToday }.sumOf { it.amount }
        val yesterdaySpent = shopping.filter { it.dateTimestamp in startOfYesterday..endOfYesterday }.sumOf { it.amount }
        val todayDiff = todaySpent - yesterdaySpent

        // 2. This Week vs Last Week (Monday-Sunday)
        val calWeek = Calendar.getInstance()
        calWeek.timeInMillis = now
        calWeek.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = calWeek.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        calWeek.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        calWeek.set(Calendar.HOUR_OF_DAY, 0)
        calWeek.set(Calendar.MINUTE, 0)
        calWeek.set(Calendar.SECOND, 0)
        calWeek.set(Calendar.MILLISECOND, 0)
        val startOfThisWeek = calWeek.timeInMillis
        val endOfThisWeek = startOfThisWeek + 7 * 24 * 3600 * 1000L - 1
        val startOfLastWeek = startOfThisWeek - 7 * 24 * 3600 * 1000L
        val endOfLastWeek = startOfThisWeek - 1

        val thisWeekSpent = shopping.filter { it.dateTimestamp in startOfThisWeek..endOfThisWeek }.sumOf { it.amount }
        val lastWeekSpent = shopping.filter { it.dateTimestamp in startOfLastWeek..endOfLastWeek }.sumOf { it.amount }
        val weekDiff = thisWeekSpent - lastWeekSpent

        // 3. This Month vs Last Month
        val thisMonthSpent = shopping.filter { it.monthYear == currentMonthYear }.sumOf { it.amount }
        val prevMonthYear = getPreviousMonth(currentMonthYear)
        val lastMonthSpent = shopping.filter { it.monthYear == prevMonthYear }.sumOf { it.amount }
        val monthDiff = thisMonthSpent - lastMonthSpent

        return SwipeableSpendingSummary(
            monthSpending = PeriodSpending(
                title = "Total spent this month",
                currentAmount = thisMonthSpent,
                previousAmount = lastMonthSpent,
                diffAmount = monthDiff,
                periodLabel = "than last month"
            ),
            weekSpending = PeriodSpending(
                title = "Total spent this week",
                currentAmount = thisWeekSpent,
                previousAmount = lastWeekSpent,
                diffAmount = weekDiff,
                periodLabel = "than last week"
            ),
            todaySpending = PeriodSpending(
                title = "Total spent today",
                currentAmount = todaySpent,
                previousAmount = yesterdaySpent,
                diffAmount = todayDiff,
                periodLabel = "than yesterday"
            )
        )
    }

    private fun getPreviousMonth(monthYear: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = sdf.parse(monthYear) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            sdf.format(cal.time)
        } catch (e: Exception) {
            monthYear
        }
    }

    suspend fun getExpensesForCategory(categoryId: Long, monthYear: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForCategory(categoryId, monthYear)
    }
}
