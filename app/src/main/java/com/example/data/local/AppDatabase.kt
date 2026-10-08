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
        ExpenseEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao

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
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigrationFrom(1, 2, 3)
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
                        }
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val householdDao = database.householdDao()
            val categoryDao = database.categoryDao()

            // 1. Initial Household Tarik
            householdDao.insertHousehold(
                HouseholdEntity(
                    id = 1,
                    name = "Tarik",
                    currency = "MAD"
                )
            )

            // 2. Default Categories: Rent, Sport, Alimentation, Water Bill, Electricity Bill, Internet Bill, Family
            val defaultCategories = listOf(
                CategoryEntity(
                    id = 1,
                    name = "Rent",
                    iconKey = "rent",
                    isRecurring = true,
                    colorHex = 0xFF10B981,
                    displayOrder = 0
                ),
                CategoryEntity(
                    id = 2,
                    name = "Sport",
                    iconKey = "sport",
                    isRecurring = true,
                    colorHex = 0xFF06B6D4,
                    displayOrder = 1
                ),
                CategoryEntity(
                    id = 3,
                    name = "Alimentation",
                    iconKey = "alimentation",
                    isRecurring = true,
                    colorHex = 0xFFF59E0B,
                    displayOrder = 2
                ),
                CategoryEntity(
                    id = 4,
                    name = "Water Bill",
                    iconKey = "water",
                    isRecurring = true,
                    colorHex = 0xFF3B82F6,
                    displayOrder = 3
                ),
                CategoryEntity(
                    id = 5,
                    name = "Electricity Bill",
                    iconKey = "electricity",
                    isRecurring = true,
                    colorHex = 0xFFEAB308,
                    displayOrder = 4
                ),
                CategoryEntity(
                    id = 6,
                    name = "Internet Bill",
                    iconKey = "internet",
                    isRecurring = true,
                    colorHex = 0xFF8B5CF6,
                    displayOrder = 5
                ),
                CategoryEntity(
                    id = 7,
                    name = "Family",
                    iconKey = "family",
                    isRecurring = true,
                    colorHex = 0xFFEC4899,
                    displayOrder = 6
                )
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
