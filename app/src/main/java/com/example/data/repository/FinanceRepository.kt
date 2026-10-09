package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(
    private val householdDao: HouseholdDao,
    private val categoryDao: CategoryDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val database: AppDatabase? = null
) {
    val primaryHousehold: Flow<HouseholdEntity?> = householdDao.getPrimaryHousehold()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun ensureDefaultData() {
        // Categories and household are initialized by Room DatabaseCallback
    }

    suspend fun updateHouseholdName(id: Long, name: String, currency: String = "MAD") {
        householdDao.updateHousehold(HouseholdEntity(id = id, name = name, currency = currency))
    }

    fun getActiveCategoriesForMonth(monthYear: String): Flow<List<CategoryEntity>> {
        return categoryDao.getActiveCategoriesForMonth(monthYear)
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

    suspend fun deleteCategoryFromMonth(category: CategoryEntity, monthYear: String) {
        if (database != null) {
            database.withTransaction {
                performDeleteCategoryFromMonth(category, monthYear)
            }
        } else {
            performDeleteCategoryFromMonth(category, monthYear)
        }
    }

    private suspend fun performDeleteCategoryFromMonth(category: CategoryEntity, monthYear: String) {
        if (monthYear > category.activeFromMonth) {
            val prevMonth = com.example.util.AppConstants.addMonths(monthYear, -1)
            categoryDao.updateCategoryActiveUntilMonth(category.id, prevMonth)
            expenseDao.deleteExpensesForCategoryFromMonth(category.id, monthYear)
        } else {
            // monthYear == category.activeFromMonth (or before)
            categoryDao.deleteCategory(category)
            expenseDao.deleteExpensesForCategoryFromMonth(category.id, monthYear)
        }
    }

    suspend fun getExpenseCountForCategory(categoryId: Long): Int {
        return expenseDao.getExpenseCountForCategory(categoryId)
    }

    suspend fun getExpenseCountForCategoryFromMonth(categoryId: Long, fromMonth: String): Int {
        return expenseDao.getExpenseCountForCategoryFromMonth(categoryId, fromMonth)
    }

    fun getIncomesForMonth(monthYear: String): Flow<List<IncomeEntity>> {
        return incomeDao.getIncomesForMonth(monthYear)
    }

    suspend fun getIncomesForMonthOnce(monthYear: String): List<IncomeEntity> {
        return incomeDao.getIncomesForMonthList(monthYear)
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

    /**
     * Coverage logic for multi-month advance payments (coversMonths > 1):
     * A month M is "covered" for a category if there is an expense of that category with coversMonths > 1
     * such that M is strictly after the expense's monthYear AND M <= expense.monthYear + (coversMonths - 1) months,
     * AND the category has no expenses of its own in month M.
     * If multiple expenses cover M, use the one with the latest end month.
     */
    fun getCoveredCategoriesForMonth(monthYear: String): Flow<Map<Long, CategoryCoverage>> {
        return expenseDao.getAllExpenses().map { allExpenses ->
            val expensesInSelectedMonth = allExpenses.filter { it.monthYear == monthYear }
            val categoriesWithExpensesInMonth = expensesInSelectedMonth
                .map { it.categoryId }
                .filter { it != 0L }
                .toSet()

            // Candidates: expenses with coversMonths > 1 for real categories where monthYear is covered
            val candidateExpenses = allExpenses.filter { exp ->
                exp.categoryId != 0L &&
                exp.coversMonths > 1 &&
                monthYear > exp.monthYear &&
                monthYear <= com.example.util.AppConstants.addMonths(exp.monthYear, exp.coversMonths - 1) &&
                !categoriesWithExpensesInMonth.contains(exp.categoryId)
            }

            // Group by categoryId, pick the one with latest end month
            val coverageMap = mutableMapOf<Long, CategoryCoverage>()
            val groupedByCat = candidateExpenses.groupBy { it.categoryId }
            for ((catId, expList) in groupedByCat) {
                val bestExpense = expList.maxByOrNull { exp ->
                    com.example.util.AppConstants.addMonths(exp.monthYear, exp.coversMonths - 1)
                }
                if (bestExpense != null) {
                    val endMonth = com.example.util.AppConstants.addMonths(bestExpense.monthYear, bestExpense.coversMonths - 1)
                    coverageMap[catId] = CategoryCoverage(
                        categoryId = catId,
                        coveringExpense = bestExpense,
                        endMonthYear = endMonth
                    )
                }
            }
            coverageMap
        }
    }

    fun getCategorySpendingForMonth(monthYear: String): Flow<List<CategoryWithSpent>> {
        return combine(
            categoryDao.getActiveCategoriesForMonth(monthYear),
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

    suspend fun exportBackupJson(): String {
        val households = householdDao.getAllHouseholdsList()
        val categories = categoryDao.getCategoriesList()
        val incomes = incomeDao.getAllIncomesList()
        val expenses = expenseDao.getAllExpensesList()

        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Income Control")
        root.put("exportedAt", System.currentTimeMillis())

        val householdsArray = JSONArray()
        for (h in households) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("currency", h.currency)
            obj.put("createdAt", h.createdAt)
            householdsArray.put(obj)
        }
        root.put("households", householdsArray)

        val categoriesArray = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("iconKey", c.iconKey)
            obj.put("colorHex", c.colorHex)
            obj.put("isRecurring", c.isRecurring)
            obj.put("householdId", c.householdId)
            obj.put("displayOrder", c.displayOrder)
            obj.put("activeFromMonth", c.activeFromMonth)
            if (c.activeUntilMonth != null) {
                obj.put("activeUntilMonth", c.activeUntilMonth)
            } else {
                obj.put("activeUntilMonth", JSONObject.NULL)
            }
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        val incomesArray = JSONArray()
        for (inc in incomes) {
            val obj = JSONObject()
            obj.put("id", inc.id)
            obj.put("source", inc.source)
            obj.put("amount", inc.amount)
            obj.put("monthYear", inc.monthYear)
            obj.put("isRecurring", inc.isRecurring)
            obj.put("dateTimestamp", inc.dateTimestamp)
            obj.put("householdId", inc.householdId)
            obj.put("note", inc.note)
            incomesArray.put(obj)
        }
        root.put("incomes", incomesArray)

        val expensesArray = JSONArray()
        for (exp in expenses) {
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("title", exp.title)
            obj.put("amount", exp.amount)
            obj.put("categoryId", exp.categoryId)
            obj.put("categoryName", exp.categoryName)
            obj.put("categoryIconKey", exp.categoryIconKey)
            obj.put("dateTimestamp", exp.dateTimestamp)
            obj.put("monthYear", exp.monthYear)
            obj.put("note", exp.note)
            obj.put("isRecurring", exp.isRecurring)
            obj.put("householdId", exp.householdId)
            obj.put("coversMonths", exp.coversMonths)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("households") && !root.has("categories") && !root.has("expenses")) {
                return Result.failure(IllegalArgumentException("Invalid backup file: missing required data"))
            }

            val householdsList = mutableListOf<HouseholdEntity>()
            if (root.has("households")) {
                val array = root.getJSONArray("households")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    householdsList.add(
                        HouseholdEntity(
                            id = obj.optLong("id", 1L),
                            name = obj.optString("name", "Tarik"),
                            currency = obj.optString("currency", "MAD"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val categoriesList = mutableListOf<CategoryEntity>()
            if (root.has("categories")) {
                val array = root.getJSONArray("categories")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val activeUntilRaw = if (obj.has("activeUntilMonth") && !obj.isNull("activeUntilMonth")) {
                        obj.optString("activeUntilMonth", "").takeIf { it.isNotBlank() }
                    } else null

                    categoriesList.add(
                        CategoryEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            iconKey = obj.optString("iconKey", "shopping"),
                            colorHex = obj.optLong("colorHex", 0xFF10B981),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            householdId = obj.optLong("householdId", 1L),
                            displayOrder = obj.optInt("displayOrder", i),
                            activeFromMonth = obj.optString("activeFromMonth", "0000-01"),
                            activeUntilMonth = activeUntilRaw
                        )
                    )
                }
            }

            val incomesList = mutableListOf<IncomeEntity>()
            if (root.has("incomes")) {
                val array = root.getJSONArray("incomes")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    incomesList.add(
                        IncomeEntity(
                            id = obj.optLong("id", 0L),
                            source = obj.optString("source", "Monthly Income"),
                            amount = obj.getDouble("amount"),
                            monthYear = obj.optString("monthYear", "2026-10"),
                            isRecurring = obj.optBoolean("isRecurring", true),
                            dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis()),
                            householdId = obj.optLong("householdId", 1L),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            val expensesList = mutableListOf<ExpenseEntity>()
            if (root.has("expenses")) {
                val array = root.getJSONArray("expenses")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    expensesList.add(
                        ExpenseEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            categoryId = obj.optLong("categoryId", 0L),
                            categoryName = obj.optString("categoryName", "Separate Purchase"),
                            categoryIconKey = obj.optString("categoryIconKey", "shopping"),
                            dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis()),
                            monthYear = obj.optString("monthYear", "2026-10"),
                            note = obj.optString("note", ""),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            householdId = obj.optLong("householdId", 1L),
                            coversMonths = obj.optInt("coversMonths", 1)
                        )
                    )
                }
            }

            if (database != null) {
                database.withTransaction {
                    householdDao.clearAllHouseholds()
                    categoryDao.clearAllCategories()
                    incomeDao.clearAllIncomes()
                    expenseDao.clearAllExpenses()

                    if (householdsList.isNotEmpty()) householdDao.insertHouseholds(householdsList)
                    if (categoriesList.isNotEmpty()) categoryDao.insertCategories(categoriesList)
                    if (incomesList.isNotEmpty()) incomeDao.insertIncomes(incomesList)
                    if (expensesList.isNotEmpty()) expenseDao.insertExpenses(expensesList)
                }
            } else {
                householdDao.clearAllHouseholds()
                categoryDao.clearAllCategories()
                incomeDao.clearAllIncomes()
                expenseDao.clearAllExpenses()

                if (householdsList.isNotEmpty()) householdDao.insertHouseholds(householdsList)
                if (categoriesList.isNotEmpty()) categoryDao.insertCategories(categoriesList)
                if (incomesList.isNotEmpty()) incomeDao.insertIncomes(incomesList)
                if (expensesList.isNotEmpty()) expenseDao.insertExpenses(expensesList)
            }

            Result.success("Restored: ${categoriesList.size} categories, ${incomesList.size} incomes, ${expensesList.size} expenses")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
