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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
