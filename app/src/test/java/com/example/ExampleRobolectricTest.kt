package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.HouseholdEntity
import com.example.data.model.IncomeEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinanceRepository(
            householdDao = db.householdDao(),
            categoryDao = db.categoryDao(),
            incomeDao = db.incomeDao(),
            expenseDao = db.expenseDao(),
            database = db
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testReadStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Income Control", appName)
    }

    @Test
    fun testBackupExportAndImportRoundTrip() = runBlocking {
        // 1. Populate sample data
        val testHousehold = HouseholdEntity(id = 1, name = "Tarik Test", currency = "MAD")
        db.householdDao().insertHousehold(testHousehold)

        val testCategory = CategoryEntity(
            id = 10,
            name = "TestCategory",
            iconKey = "sport",
            isRecurring = true,
            colorHex = 0xFF10B981,
            displayOrder = 1
        )
        db.categoryDao().insertCategory(testCategory)

        val testIncome = IncomeEntity(
            id = 5,
            source = "Monthly Salary",
            amount = 12000.0,
            monthYear = "2026-10",
            isRecurring = true,
            dateTimestamp = 1791280000000L
        )
        db.incomeDao().insertIncome(testIncome)

        val testExpense = ExpenseEntity(
            id = 20,
            title = "Gym Membership",
            amount = 300.0,
            categoryId = 10,
            categoryName = "TestCategory",
            categoryIconKey = "sport",
            dateTimestamp = 1791280000000L,
            monthYear = "2026-10",
            note = "Annual subscription installment",
            isRecurring = true,
            coversMonths = 3
        )
        db.expenseDao().insertExpense(testExpense)

        // 2. Export to JSON
        val exportedJson = repository.exportBackupJson()
        assertNotNull(exportedJson)
        assertTrue(exportedJson.contains("Tarik Test"))
        assertTrue(exportedJson.contains("TestCategory"))
        assertTrue(exportedJson.contains("Gym Membership"))
        assertTrue(exportedJson.contains("Monthly Salary"))
        assertTrue(exportedJson.contains("coversMonths") && exportedJson.contains("3"))

        // 3. Clear database to simulate data restore into empty or new state
        db.expenseDao().clearAllExpenses()
        db.incomeDao().clearAllIncomes()
        db.categoryDao().deleteCategory(testCategory)

        assertEquals(0, db.expenseDao().getAllExpensesList().size)

        // 4. Import from the exported JSON
        val restoreResult = repository.importBackupJson(exportedJson)
        assertTrue(restoreResult.isSuccess)

        // 5. Verify restored items
        val restoredExpenses = db.expenseDao().getAllExpensesList()
        assertEquals(1, restoredExpenses.size)
        assertEquals("Gym Membership", restoredExpenses[0].title)
        assertEquals(300.0, restoredExpenses[0].amount, 0.001)
        assertEquals(10L, restoredExpenses[0].categoryId)
        assertEquals(3, restoredExpenses[0].coversMonths)

        val restoredCategories = db.categoryDao().getCategoriesList()
        val restoredCategory = restoredCategories.find { it.id == 10L }
        assertNotNull(restoredCategory)
        assertEquals("TestCategory", restoredCategory?.name)

        val restoredHouseholds = db.householdDao().getAllHouseholdsList()
        val restoredHousehold = restoredHouseholds.firstOrNull()
        assertNotNull(restoredHousehold)
        assertEquals("Tarik Test", restoredHousehold?.name)
    }

    @Test
    fun testGetCoveredCategoriesForMonth() = runBlocking {
        // Setup: Sport category (id = 42)
        val sportCategory = CategoryEntity(
            id = 42,
            name = "Sport",
            iconKey = "sport",
            isRecurring = true,
            colorHex = 0xFF10B981,
            displayOrder = 1
        )
        db.categoryDao().insertCategory(sportCategory)

        // Expense paid in 2026-10 with coversMonths = 3 (covers Oct 2026, Nov 2026, Dec 2026)
        val sportExpense = ExpenseEntity(
            id = 100,
            title = "Sport Subscription",
            amount = 400.0,
            categoryId = 42,
            categoryName = "Sport",
            categoryIconKey = "sport",
            dateTimestamp = 1791280000000L,
            monthYear = "2026-10",
            note = "3 months subscription",
            isRecurring = true,
            coversMonths = 3
        )
        db.expenseDao().insertExpense(sportExpense)

        // 1. In payment month (2026-10): NOT covered (the expense is paid directly in this month)
        val octCoverage = repository.getCoveredCategoriesForMonth("2026-10").first()
        assertNull("Payment month should not be marked covered", octCoverage[42L])

        // 2. In 2026-11: COVERED (endMonthYear = 2026-12)
        val novCoverage = repository.getCoveredCategoriesForMonth("2026-11").first()
        assertNotNull("November should be covered", novCoverage[42L])
        assertEquals("2026-12", novCoverage[42L]?.endMonthYear)
        assertEquals(100L, novCoverage[42L]?.coveringExpense?.id)

        // 3. In 2026-12: COVERED (endMonthYear = 2026-12)
        val decCoverage = repository.getCoveredCategoriesForMonth("2026-12").first()
        assertNotNull("December should be covered", decCoverage[42L])
        assertEquals("2026-12", decCoverage[42L]?.endMonthYear)

        // 4. In 2027-01: NOT covered (coverage ended in Dec 2026)
        val janCoverage = repository.getCoveredCategoriesForMonth("2027-01").first()
        assertNull("January should not be covered", janCoverage[42L])

        // 5. If the category has its own expense in 2026-11, it is NOT covered in 2026-11
        val extraNovExpense = ExpenseEntity(
            id = 101,
            title = "Extra Sport Session",
            amount = 50.0,
            categoryId = 42,
            categoryName = "Sport",
            categoryIconKey = "sport",
            dateTimestamp = 1793872000000L,
            monthYear = "2026-11",
            note = "",
            isRecurring = true,
            coversMonths = 1
        )
        db.expenseDao().insertExpense(extraNovExpense)

        val novCoverageWithOwnExpense = repository.getCoveredCategoriesForMonth("2026-11").first()
        assertNull("Month with category's own expense should not be covered", novCoverageWithOwnExpense[42L])

        // December should still be covered
        val decCoverageStill = repository.getCoveredCategoriesForMonth("2026-12").first()
        assertNotNull("December should still be covered", decCoverageStill[42L])
    }

    @Test
    fun testMigration5To6() {
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
            ApplicationProvider.getApplicationContext()
        )
            .name(null)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS categories (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            iconKey TEXT NOT NULL,
                            isRecurring INTEGER NOT NULL,
                            displayOrder INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS incomes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            source TEXT NOT NULL,
                            amount REAL NOT NULL,
                            monthYear TEXT NOT NULL,
                            isRecurring INTEGER NOT NULL,
                            dateTimestamp INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO categories (id, name, iconKey, isRecurring, displayOrder)
                        VALUES (1, 'Groceries', 'cart', 1, 0)
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO incomes (id, source, amount, monthYear, isRecurring, dateTimestamp)
                        VALUES (1, 'Salary', 5000.0, '2026-10', 1, 1700000000000)
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

        com.example.data.local.MIGRATION_5_6.migrate(supportDb)

        val catCursor = supportDb.query("SELECT id, name, activeFromMonth, activeUntilMonth FROM categories WHERE id = 1")
        assertTrue(catCursor.moveToFirst())
        assertEquals("0000-01", catCursor.getString(catCursor.getColumnIndexOrThrow("activeFromMonth")))
        assertTrue(catCursor.isNull(catCursor.getColumnIndexOrThrow("activeUntilMonth")))
        catCursor.close()

        val incCursor = supportDb.query("SELECT id, source, note FROM incomes WHERE id = 1")
        assertTrue(incCursor.moveToFirst())
        assertEquals("", incCursor.getString(incCursor.getColumnIndexOrThrow("note")))
        incCursor.close()

        supportDb.close()
    }

    @Test
    fun testDeleteCategoryFromMonth() = runBlocking {
        // category active from 2026-08
        val cat = CategoryEntity(
            id = 50,
            name = "Gym",
            iconKey = "sport",
            isRecurring = true,
            displayOrder = 0,
            activeFromMonth = "2026-08",
            activeUntilMonth = null
        )
        db.categoryDao().insertCategory(cat)

        // insert expenses in 2026-08, 2026-09, 2026-10, 2026-11
        db.expenseDao().insertExpense(ExpenseEntity(id = 501, title = "Gym Aug", amount = 100.0, categoryId = 50, categoryName = "Gym", categoryIconKey = "sport", dateTimestamp = 1000L, monthYear = "2026-08"))
        db.expenseDao().insertExpense(ExpenseEntity(id = 502, title = "Gym Sep", amount = 100.0, categoryId = 50, categoryName = "Gym", categoryIconKey = "sport", dateTimestamp = 2000L, monthYear = "2026-09"))
        db.expenseDao().insertExpense(ExpenseEntity(id = 503, title = "Gym Oct", amount = 100.0, categoryId = 50, categoryName = "Gym", categoryIconKey = "sport", dateTimestamp = 3000L, monthYear = "2026-10"))
        db.expenseDao().insertExpense(ExpenseEntity(id = 504, title = "Gym Nov", amount = 100.0, categoryId = 50, categoryName = "Gym", categoryIconKey = "sport", dateTimestamp = 4000L, monthYear = "2026-11"))

        // delete from 2026-10
        repository.deleteCategoryFromMonth(cat, "2026-10")

        val updatedCat = db.categoryDao().getCategoriesList().first { it.id == 50L }
        assertEquals("2026-09", updatedCat.activeUntilMonth)

        // Expenses of 2026-08 and 2026-09 remain
        val expensesList = db.expenseDao().getAllExpensesList()
        assertNotNull(expensesList.find { it.id == 501L })
        assertNotNull(expensesList.find { it.id == 502L })
        // Expenses of 2026-10+ deleted
        assertNull(expensesList.find { it.id == 503L })
        assertNull(expensesList.find { it.id == 504L })

        // appears in getActiveCategoriesForMonth("2026-09") but not in "2026-10"
        val activeSep = repository.getActiveCategoriesForMonth("2026-09").first()
        assertTrue(activeSep.any { it.id == 50L })
        val activeOct = repository.getActiveCategoriesForMonth("2026-10").first()
        assertTrue(activeOct.none { it.id == 50L })
    }

    @Test
    fun testCategoryCreatedInMonthNotReturnedInPreviousMonth() = runBlocking {
        // A category created in 2026-10 must NOT be returned by getActiveCategoriesForMonth("2026-09")
        val cat = CategoryEntity(
            id = 60,
            name = "Yoga",
            iconKey = "sport",
            isRecurring = true,
            displayOrder = 1,
            activeFromMonth = "2026-10",
            activeUntilMonth = null
        )
        db.categoryDao().insertCategory(cat)

        val activeSep = repository.getActiveCategoriesForMonth("2026-09").first()
        assertTrue(activeSep.none { it.id == 60L })

        val activeOct = repository.getActiveCategoriesForMonth("2026-10").first()
        assertTrue(activeOct.any { it.id == 60L })
    }

    @Test
    fun testBackupRoundTripWithActiveUntilMonthAndIncomeNote() = runBlocking {
        val cat = CategoryEntity(
            id = 70,
            name = "Club",
            iconKey = "sport",
            isRecurring = true,
            displayOrder = 0,
            activeFromMonth = "2026-01",
            activeUntilMonth = "2026-05"
        )
        db.categoryDao().insertCategory(cat)

        val inc = IncomeEntity(
            id = 71,
            source = "Freelance",
            amount = 1200.0,
            monthYear = "2026-03",
            isRecurring = false,
            dateTimestamp = 1700000000000L,
            note = "Project milestone bonus"
        )
        db.incomeDao().insertIncome(inc)

        val exportedJson = repository.exportBackupJson()
        assertTrue(exportedJson.contains("2026-05"))
        assertTrue(exportedJson.contains("Project milestone bonus"))

        // Clear DB and restore
        db.categoryDao().deleteCategory(cat)
        db.incomeDao().deleteIncome(inc)

        val restoreResult = repository.importBackupJson(exportedJson)
        assertTrue(restoreResult.isSuccess)

        val restoredCats = db.categoryDao().getCategoriesList()
        val restoredCat = restoredCats.firstOrNull { it.id == 70L }
        assertNotNull(restoredCat)
        assertEquals("2026-01", restoredCat?.activeFromMonth)
        assertEquals("2026-05", restoredCat?.activeUntilMonth)

        val restoredIncomes = db.incomeDao().getIncomesForMonthList("2026-03")
        val restoredInc = restoredIncomes.firstOrNull { it.id == 71L }
        assertNotNull(restoredInc)
        assertEquals("Project milestone bonus", restoredInc?.note)
    }
}
