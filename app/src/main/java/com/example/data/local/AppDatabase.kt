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
    version = 1,
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
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val householdDao = database.householdDao()
            val categoryDao = database.categoryDao()
            val budgetDao = database.budgetDao()
            val incomeDao = database.incomeDao()
            val expenseDao = database.expenseDao()

            // 1. Initial Household Tarik
            householdDao.insertHousehold(
                HouseholdEntity(
                    id = 1,
                    name = "Tarik",
                    currency = "MAD"
                )
            )

            // 2. Default Categories matching the screenshots:
            // Rent, Sport, Alimentation (recurring), Water Bill, Electricity Bill, Internet Bill, Family
            val defaultCategories = listOf(
                CategoryEntity(
                    id = 1,
                    name = "Rent",
                    iconKey = "rent",
                    isRecurring = true,
                    defaultPlannedAmount = 3000.0,
                    colorHex = 0xFF10B981
                ),
                CategoryEntity(
                    id = 2,
                    name = "Sport",
                    iconKey = "sport",
                    isRecurring = true,
                    defaultPlannedAmount = 300.0,
                    colorHex = 0xFF06B6D4
                ),
                CategoryEntity(
                    id = 3,
                    name = "Alimentation",
                    iconKey = "alimentation",
                    isRecurring = true,
                    defaultPlannedAmount = 2500.0,
                    colorHex = 0xFFF59E0B
                ),
                CategoryEntity(
                    id = 4,
                    name = "Water Bill",
                    iconKey = "water",
                    isRecurring = true,
                    defaultPlannedAmount = 150.0,
                    colorHex = 0xFF3B82F6
                ),
                CategoryEntity(
                    id = 5,
                    name = "Electricity Bill",
                    iconKey = "electricity",
                    isRecurring = true,
                    defaultPlannedAmount = 250.0,
                    colorHex = 0xFFEAB308
                ),
                CategoryEntity(
                    id = 6,
                    name = "Internet Bill",
                    iconKey = "internet",
                    isRecurring = true,
                    defaultPlannedAmount = 250.0,
                    colorHex = 0xFF8B5CF6
                ),
                CategoryEntity(
                    id = 7,
                    name = "Family",
                    iconKey = "family",
                    isRecurring = true,
                    defaultPlannedAmount = 600.0,
                    colorHex = 0xFFEC4899
                )
            )
            categoryDao.insertCategories(defaultCategories)

            // 3. Initial Budgets for current month October 2026
            val currentMonth = "2026-10"
            val initialBudgets = defaultCategories.map {
                BudgetEntity(
                    categoryId = it.id,
                    monthYear = currentMonth,
                    plannedAmount = it.defaultPlannedAmount
                )
            }
            budgetDao.insertBudgets(initialBudgets)

            // 4. Sample initial Monthly Income (e.g. 12,000 MAD)
            incomeDao.insertIncome(
                IncomeEntity(
                    id = 1,
                    source = "Main Monthly Income",
                    amount = 12000.0,
                    monthYear = currentMonth,
                    isRecurring = true
                )
            )

            // 5. Initial sample everyday shopping expenses mentioned by user
            // ("fast food you eat outside the house the water bottle and the other things you may get from the super market")
            val now = System.currentTimeMillis()
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Mineral Water Bottle",
                    amount = 6.0,
                    categoryId = 3,
                    categoryName = "Alimentation",
                    categoryIconKey = "alimentation",
                    dateTimestamp = now - 3600_000 * 2,
                    monthYear = currentMonth,
                    note = "Cold mineral water on the walk",
                    isRecurring = false
                )
            )
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Fast Food Burger Outside",
                    amount = 55.0,
                    categoryId = 3,
                    categoryName = "Alimentation",
                    categoryIconKey = "alimentation",
                    dateTimestamp = now - 3600_000 * 24,
                    monthYear = currentMonth,
                    note = "Dinner outside with friends",
                    isRecurring = false
                )
            )
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Supermarket Groceries & Essentials",
                    amount = 320.0,
                    categoryId = 3,
                    categoryName = "Alimentation",
                    categoryIconKey = "alimentation",
                    dateTimestamp = now - 3600_000 * 48,
                    monthYear = currentMonth,
                    note = "Weekly groceries, fruits & coffee",
                    isRecurring = false
                )
            )
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Gym Monthly Subscription",
                    amount = 300.0,
                    categoryId = 2,
                    categoryName = "Sport",
                    categoryIconKey = "sport",
                    dateTimestamp = now - 3600_000 * 72,
                    monthYear = currentMonth,
                    note = "October fitness pass",
                    isRecurring = true
                )
            )
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Fibre Optic Internet Bill",
                    amount = 250.0,
                    categoryId = 6,
                    categoryName = "Internet Bill",
                    categoryIconKey = "internet",
                    dateTimestamp = now - 3600_000 * 80,
                    monthYear = currentMonth,
                    note = "Home high speed WiFi",
                    isRecurring = true
                )
            )
        }
    }
}
