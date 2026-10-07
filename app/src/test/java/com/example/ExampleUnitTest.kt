package com.example

import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.CategoryExpenseBreakdown
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppConstants
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testItemEditableWindow() {
        val now = System.currentTimeMillis()

        // 1. Inside 24h -> editable
        assertTrue(AppConstants.isItemEditable(now - 1000L * 3600 * 2, now))
        assertTrue(AppConstants.isItemEditable(now - 1000L * 3600 * 23, now))
        assertTrue(AppConstants.isItemEditable(now, now))

        // 2. After 24h -> locked (not editable)
        assertFalse(AppConstants.isItemEditable(now - 1000L * 3600 * 25, now))
        assertFalse(AppConstants.isItemEditable(now - 1000L * 3600 * 48, now))

        // 3. Future timestamp -> not editable
        assertFalse(AppConstants.isItemEditable(now + 1000L * 3600 * 2, now))
    }

    @Test
    fun testGroupExpensesByDay() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = cal.timeInMillis

        val expense1 = ExpenseEntity(
            id = 1,
            title = "Coffee",
            amount = 15.0,
            categoryId = 0,
            categoryName = "Separate Purchase",
            categoryIconKey = "shopping",
            dateTimestamp = now,
            monthYear = "2026-10",
            note = "",
            isRecurring = false
        )
        val expense2 = ExpenseEntity(
            id = 2,
            title = "Lunch",
            amount = 35.0,
            categoryId = 0,
            categoryName = "Separate Purchase",
            categoryIconKey = "shopping",
            dateTimestamp = now - 1000L * 60, // also today
            monthYear = "2026-10",
            note = "",
            isRecurring = false
        )
        val expense3 = ExpenseEntity(
            id = 3,
            title = "Groceries",
            amount = 50.0,
            categoryId = 0,
            categoryName = "Separate Purchase",
            categoryIconKey = "shopping",
            dateTimestamp = yesterday,
            monthYear = "2026-10",
            note = "",
            isRecurring = false
        )

        val groups = FinanceViewModel.groupExpensesByDay(listOf(expense1, expense2, expense3))
        assertEquals(2, groups.size)

        val todayGroup = groups.first { it.dayTitle == "Today" }
        assertEquals(50.0, todayGroup.dayTotal, 0.001)
        assertEquals(2, todayGroup.expenses.size)

        val yesterdayGroup = groups.first { it.dayTitle == "Yesterday" }
        assertEquals(50.0, yesterdayGroup.dayTotal, 0.001)
        assertEquals(1, yesterdayGroup.expenses.size)
    }

    @Test
    fun testCategoryBreakdownPercentagesSumTo100WithSeparatePurchases() {
        val category1 = CategoryEntity(
            id = 1,
            name = "Rent",
            iconKey = "rent",
            isRecurring = true,
            colorHex = 0xFF10B981,
            displayOrder = 0
        )
        val categories = listOf(category1)
        val categoryMap = categories.associateBy { it.id }

        // One category expense (60.0) and one separate purchase (40.0) with categoryId = 0
        val expenses = listOf(
            ExpenseEntity(
                id = 1,
                title = "Monthly Rent",
                amount = 60.0,
                categoryId = 1,
                categoryName = "Rent",
                categoryIconKey = "rent",
                dateTimestamp = System.currentTimeMillis(),
                monthYear = "2026-10",
                note = "",
                isRecurring = true
            ),
            ExpenseEntity(
                id = 2,
                title = "Supermarket",
                amount = 40.0,
                categoryId = 0,
                categoryName = "Separate Purchase",
                categoryIconKey = "shopping",
                dateTimestamp = System.currentTimeMillis(),
                monthYear = "2026-10",
                note = "",
                isRecurring = false
            )
        )

        val total = expenses.sumOf { it.amount }
        val knownCategoryExpenses = mutableMapOf<CategoryEntity, MutableList<ExpenseEntity>>()
        val separatePurchases = mutableListOf<ExpenseEntity>()

        for (expense in expenses) {
            val cat = categoryMap[expense.categoryId]
            if (cat != null) {
                knownCategoryExpenses.getOrPut(cat) { mutableListOf() }.add(expense)
            } else {
                separatePurchases.add(expense)
            }
        }

        val list = mutableListOf<CategoryExpenseBreakdown>()
        for ((cat, catExpenses) in knownCategoryExpenses) {
            val sum = catExpenses.sumOf { it.amount }
            val pct = if (total > 0) ((sum / total) * 100).toFloat() else 0f
            list.add(
                CategoryExpenseBreakdown(
                    category = cat,
                    totalAmount = sum,
                    percentage = pct,
                    count = catExpenses.size
                )
            )
        }

        if (separatePurchases.isNotEmpty()) {
            val separateCategory = CategoryEntity(
                id = 0L,
                name = "Separate Purchases",
                iconKey = "shopping",
                isRecurring = false,
                colorHex = 0xFF64748BL,
                householdId = 1,
                displayOrder = 999
            )
            val sum = separatePurchases.sumOf { it.amount }
            val pct = if (total > 0) ((sum / total) * 100).toFloat() else 0f
            list.add(
                CategoryExpenseBreakdown(
                    category = separateCategory,
                    totalAmount = sum,
                    percentage = pct,
                    count = separatePurchases.size
                )
            )
        }

        assertEquals(2, list.size)
        val rentBreakdown = list.first { it.category.id == 1L }
        assertEquals(60.0f, rentBreakdown.percentage, 0.01f)

        val separateBreakdown = list.first { it.category.id == 0L }
        assertEquals(40.0f, separateBreakdown.percentage, 0.01f)

        val totalPercentage = list.sumOf { it.percentage.toDouble() }
        assertEquals(100.0, totalPercentage, 0.01)
    }

    @Test
    fun testLocalizationStrings() {
        val enStrings = AppStrings(AppLanguage.ENGLISH)
        val frStrings = AppStrings(AppLanguage.FRENCH)

        assertEquals("Settings", enStrings.settingsTitle)
        assertEquals("Paramètres", frStrings.settingsTitle)

        assertEquals("Version 1.9", enStrings.versionLabel)
        assertEquals("Version 1.9", frStrings.versionLabel)

        assertEquals(
            "Delete Rent and its 3 purchases? This cannot be undone.",
            enStrings.deleteCategoryConfirmation("Rent", 3)
        )
        assertEquals(
            "Supprimer Loyer et ses 3 achats ? Cette action est irréversible.",
            frStrings.deleteCategoryConfirmation("Loyer", 3)
        )
    }
}
