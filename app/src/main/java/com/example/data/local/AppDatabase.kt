package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HouseholdEntity::class,
        CategoryEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "income_control_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.categoryDao().getCategoryCount() == 0) {
                            populateInitialData(database)
                        } else {
                            // Ensure all estimated/planned amounts are 0.0 on existing databases as well
                            database.categoryDao().resetAllPlannedAmountsToZero()
                            database.budgetDao().resetAllBudgetsToZero()
                        }
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val householdDao = database.householdDao()
            val categoryDao = database.categoryDao()
            val budgetDao = database.budgetDao()

            // 1. Initial Household Tarik
            householdDao.insertHousehold(
                HouseholdEntity(
                    id = 1,
                    name = "Tarik",
                    currency = "MAD"
                )
            )

            // 2. Default Categories: Rent, Sport, Alimentation, Water Bill, Electricity Bill, Internet Bill, Family
            // All initialized with 0.0 estimated/planned amount as requested
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

            // 3. Initial Budgets set to 0.0 for current month October 2026
            val currentMonth = "2026-10"
            val initialBudgets = defaultCategories.map {
                BudgetEntity(
                    categoryId = it.id,
                    monthYear = currentMonth,
                    plannedAmount = 0.0
                )
            }
            budgetDao.insertBudgets(initialBudgets)
        }
    }
}
