package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
    }

    suspend fun getExpensesForCategory(categoryId: Long, monthYear: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForCategory(categoryId, monthYear)
    }
}
