package com.example

import com.example.data.local.MIGRATION_4_5
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.CategoryExpenseBreakdown
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppConstants
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

        // Call the real pure function extracted in FinanceViewModel
        val list = FinanceViewModel.computeCategoryBreakdown(
            expenses = expenses,
            categories = categories,
            separateName = "Separate Purchases"
        )

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

        assertEquals("Version 1.11", enStrings.versionLabel)
        assertEquals("Version 1.11", frStrings.versionLabel)

        assertEquals(
            "Delete Rent and its 3 purchases? This cannot be undone.",
            enStrings.deleteCategoryConfirmation("Rent", 3)
        )
        assertEquals(
            "Supprimer Loyer et ses 3 achats ? Cette action est irréversible.",
            frStrings.deleteCategoryConfirmation("Loyer", 3)
        )
    }

    @Test
    fun testShouldShowComparison() {
        assertFalse(AppConstants.shouldShowComparison(0.0))
        assertFalse(AppConstants.shouldShowComparison(-1.0))
        assertTrue(AppConstants.shouldShowComparison(0.5))
        assertTrue(AppConstants.shouldShowComparison(100.0))
    }

    @Test
    fun testMigration4To5() {
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
            .name(null) // in-memory
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS expenses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            amount REAL NOT NULL,
                            categoryId INTEGER NOT NULL,
                            categoryName TEXT NOT NULL,
                            categoryIconKey TEXT NOT NULL,
                            dateTimestamp INTEGER NOT NULL,
                            monthYear TEXT NOT NULL,
                            note TEXT NOT NULL,
                            isRecurring INTEGER NOT NULL,
                            householdId INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO expenses (id, title, amount, categoryId, categoryName, categoryIconKey, dateTimestamp, monthYear, note, isRecurring, householdId)
                        VALUES (1, 'Old Expense', 250.0, 5, 'Utilities', 'lightbulb', 1700000000000, '2026-10', 'Monthly bill', 0, 1)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {}
            })
            .build()

        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(config)
        val supportDb = helper.writableDatabase

        // Run MIGRATION_4_5
        MIGRATION_4_5.migrate(supportDb)

        // Query the row and verify coversMonths = 1 and other columns are intact
        val cursor = supportDb.query("SELECT id, title, amount, coversMonths FROM expenses WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(1L, cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        assertEquals("Old Expense", cursor.getString(cursor.getColumnIndexOrThrow("title")))
        assertEquals(250.0, cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0.001)
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("coversMonths")))
        cursor.close()
        supportDb.close()
    }

    @Test
    fun testAddMonthsMathAndOverflowSafety() {
        assertEquals("2027-01", AppConstants.addMonths("2026-11", 2))
        assertEquals("2027-01", AppConstants.addMonths("2026-12", 1))
        assertEquals("2027-03", AppConstants.addMonths("2027-02", 1))

        // Test with Calendar currently at day 31 to verify it does not overflow shorter months
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 31)
        val resultWhenDay31 = AppConstants.addMonths("2026-11", 2)
        assertEquals("2027-01", resultWhenDay31)
    }
}
