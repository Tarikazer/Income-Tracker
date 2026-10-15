package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "Tarik",
    val currency: String = "MAD",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String = "category",
    val isRecurring: Boolean = true,
    val colorHex: Long = 0xFF10B981,
    val householdId: Long = 1,
    val displayOrder: Int = 0,
    val activeFromMonth: String = "0000-01",
    val activeUntilMonth: String? = null
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val source: String = "Monthly Income",
    val amount: Double,
    val monthYear: String,
    val isRecurring: Boolean = true,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val householdId: Long = 1,
    val note: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long = 0,
    val categoryName: String = "",
    val categoryIconKey: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val monthYear: String,
    val note: String = "",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val householdId: Long = 1,
    val coversMonths: Int = 1
)

data class CategoryWithSpent(
    val category: CategoryEntity,
    val spentAmount: Double,
    val expensesCount: Int
)

data class MonthlyFinanceSummary(
    val monthYear: String,
    val totalIncome: Double,
    val totalSpent: Double,
    val shoppingSpent: Double = 0.0,
    val actualRemaining: Double = 0.0,
    val expenseCount: Int = 0,
    val daysRemainingInMonth: Int = 0
)

data class DailyExpenseGroup(
    val dayKey: String,
    val dayTitle: String,
    val dayTotal: Double,
    val expenses: List<ExpenseEntity>
)

data class PeriodSpending(
    val title: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val diffAmount: Double,
    val periodLabel: String
)

data class SwipeableSpendingSummary(
    val monthSpending: PeriodSpending,
    val weekSpending: PeriodSpending,
    val todaySpending: PeriodSpending,
    val currentPageIndex: Int = 0
) {
    val pages: List<PeriodSpending> get() = listOf(monthSpending, weekSpending, todaySpending)
}

data class CategoryCoverage(
    val categoryId: Long,
    val coveringExpense: ExpenseEntity,
    val endMonthYear: String
)
