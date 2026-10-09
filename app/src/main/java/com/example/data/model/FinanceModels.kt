package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Tarik",
    val currency: String = "MAD",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String, // "rent", "sport", "alimentation", "water", "electricity", "internet", "family", "transport", "other"
    val isRecurring: Boolean = true,
    val colorHex: Long = 0xFF10B981,
    val householdId: Long = 1,
    val displayOrder: Int = 0,
    val activeFromMonth: String = "0000-01",
    val activeUntilMonth: String? = null
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val amount: Double,
    val monthYear: String, // e.g. "2026-10"
    val isRecurring: Boolean = true,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val householdId: Long = 1,
    val note: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String,
    val categoryIconKey: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val monthYear: String, // e.g. "2026-10"
    val note: String = "",
    val isRecurring: Boolean = false,
    val householdId: Long = 1,
    val coversMonths: Int = 1
) {
    val createdAt: Long get() = dateTimestamp
}

data class CategoryWithSpent(
    val category: CategoryEntity,
    val spentAmount: Double,
    val expensesCount: Int
)

data class MonthlyFinanceSummary(
    val monthYear: String,
    val totalIncome: Double,
    val totalSpent: Double,
    val shoppingSpent: Double, // Day-to-day shopping and quick expenses
    val actualRemaining: Double // totalIncome - totalSpent
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
    val todaySpending: PeriodSpending
)

data class DailyExpenseGroup(
    val dayKey: String,
    val dayTitle: String,
    val dayTotal: Double,
    val expenses: List<ExpenseEntity>
)

data class CategoryCoverage(
    val categoryId: Long,
    val coveringExpense: ExpenseEntity,
    val endMonthYear: String // e.g. "2026-12"
)
