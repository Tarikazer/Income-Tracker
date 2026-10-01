package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM households LIMIT 1")
    fun getPrimaryHousehold(): Flow<HouseholdEntity?>

    @Query("SELECT * FROM households")
    fun getAllHouseholds(): Flow<List<HouseholdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHousehold(household: HouseholdEntity): Long

    @Update
    suspend fun updateHousehold(household: HouseholdEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("UPDATE categories SET defaultPlannedAmount = 0.0")
    suspend fun resetAllPlannedAmountsToZero()
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes WHERE monthYear = :monthYear ORDER BY dateTimestamp DESC")
    fun getIncomesForMonth(monthYear: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes ORDER BY dateTimestamp DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear ORDER BY dateTimestamp DESC")
    fun getExpensesForMonth(monthYear: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND monthYear = :monthYear ORDER BY dateTimestamp DESC")
    fun getExpensesForCategory(categoryId: Long, monthYear: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear LIMIT 1")
    suspend fun getBudgetForCategory(categoryId: Long, monthYear: String): BudgetEntity?

    @Query("UPDATE budgets SET plannedAmount = 0.0")
    suspend fun resetAllBudgetsToZero()
}
