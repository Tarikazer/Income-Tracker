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

    @Query("SELECT * FROM households")
    suspend fun getAllHouseholdsList(): List<HouseholdEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHousehold(household: HouseholdEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholds(households: List<HouseholdEntity>)

    @Update
    suspend fun updateHousehold(household: HouseholdEntity)

    @Query("DELETE FROM households")
    suspend fun clearAllHouseholds()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, id ASC")
    suspend fun getCategoriesList(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET displayOrder = :order WHERE id = :id")
    suspend fun updateCategoryOrder(id: Long, order: Int)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM categories WHERE activeFromMonth <= :monthYear AND (activeUntilMonth IS NULL OR :monthYear <= activeUntilMonth) ORDER BY displayOrder ASC, id ASC")
    fun getActiveCategoriesForMonth(monthYear: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE activeFromMonth <= :monthYear AND (activeUntilMonth IS NULL OR :monthYear <= activeUntilMonth) ORDER BY displayOrder ASC, id ASC")
    suspend fun getActiveCategoriesForMonthList(monthYear: String): List<CategoryEntity>

    @Query("UPDATE categories SET activeUntilMonth = :untilMonth WHERE id = :categoryId")
    suspend fun updateCategoryActiveUntilMonth(categoryId: Long, untilMonth: String)
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes WHERE monthYear = :monthYear ORDER BY CASE WHEN source LIKE '%Monthly Income%' THEN 0 ELSE 1 END, id ASC, dateTimestamp ASC")
    fun getIncomesForMonth(monthYear: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE monthYear = :monthYear ORDER BY CASE WHEN source LIKE '%Monthly Income%' THEN 0 ELSE 1 END, id ASC, dateTimestamp ASC")
    suspend fun getIncomesForMonthList(monthYear: String): List<IncomeEntity>

    @Query("SELECT * FROM incomes ORDER BY CASE WHEN source LIKE '%Monthly Income%' THEN 0 ELSE 1 END, id ASC, dateTimestamp ASC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes ORDER BY CASE WHEN source LIKE '%Monthly Income%' THEN 0 ELSE 1 END, id ASC, dateTimestamp ASC")
    suspend fun getAllIncomesList(): List<IncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(incomes: List<IncomeEntity>)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes")
    suspend fun clearAllIncomes()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear ORDER BY dateTimestamp DESC")
    fun getExpensesForMonth(monthYear: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND monthYear = :monthYear ORDER BY dateTimestamp DESC")
    fun getExpensesForCategory(categoryId: Long, monthYear: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    suspend fun getAllExpensesList(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses WHERE categoryId = :categoryId")
    suspend fun deleteExpensesForCategory(categoryId: Long)

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpenseCountForCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId AND monthYear >= :fromMonth")
    suspend fun getExpenseCountForCategoryFromMonth(categoryId: Long, fromMonth: String): Int

    @Query("DELETE FROM expenses WHERE categoryId = :categoryId AND monthYear >= :fromMonth")
    suspend fun deleteExpensesForCategoryFromMonth(categoryId: Long, fromMonth: String)

    @Query("UPDATE expenses SET categoryId = 0, categoryName = 'Separate Purchase', categoryIconKey = 'shopping' WHERE isRecurring = 0 AND (categoryName = 'Alimentation' OR categoryId = 3)")
    suspend fun detachShoppingFromAlimentation()
}
