package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(
    private val householdDao: HouseholdDao,
    private val categoryDao: CategoryDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao
) {
    val primaryHousehold: Flow<HouseholdEntity?> = householdDao.getPrimaryHousehold()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun ensureDefaultData() {
        // Categories and household are initialized by Room DatabaseCallback
    }

    suspend fun updateHouseholdName(id: Long, name: String, currency: String = "MAD") {
        householdDao.updateHousehold(HouseholdEntity(id = id, name = name, currency = currency))
    }

    suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        expenseDao.deleteExpensesForCategory(category.id)
        categoryDao.deleteCategory(category)
    }

    fun getIncomesForMonth(monthYear: String): Flow<List<IncomeEntity>> {
        return incomeDao.getIncomesForMonth(monthYear)
    }

    suspend fun addIncome(income: IncomeEntity): Long {
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    fun getExpensesForMonth(monthYear: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForMonth(monthYear)
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    fun getCategorySpendingForMonth(monthYear: String): Flow<List<CategoryWithSpent>> {
        return combine(
            categoryDao.getAllCategories(),
            expenseDao.getExpensesForMonth(monthYear)
        ) { categories, expenses ->
            val expensesByCategory = expenses.groupBy { it.categoryId }

            categories.map { category ->
                val catExpenses = expensesByCategory[category.id] ?: emptyList()
                val spent = catExpenses.sumOf { it.amount }
                CategoryWithSpent(
                    category = category,
                    spentAmount = spent,
                    expensesCount = catExpenses.size
                )
            }
        }
    }

    fun getMonthlySummary(monthYear: String): Flow<MonthlyFinanceSummary> {
        return combine(
            incomeDao.getIncomesForMonth(monthYear),
            expenseDao.getExpensesForMonth(monthYear)
        ) { incomes, expenses ->
            val totalIncome = incomes.sumOf { it.amount }
            val totalSpent = expenses.sumOf { it.amount }
            val shoppingSpent = expenses.filter { !it.isRecurring }.sumOf { it.amount }
            val actualRemaining = totalIncome - totalSpent

            MonthlyFinanceSummary(
                monthYear = monthYear,
                totalIncome = totalIncome,
                totalSpent = totalSpent,
                shoppingSpent = shoppingSpent,
                actualRemaining = actualRemaining
            )
        }
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

    fun getExpensesForCategory(categoryId: Long, monthYear: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForCategory(categoryId, monthYear)
    }
}
