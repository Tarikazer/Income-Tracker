package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class IncomeControlApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this, applicationScope)
    }

    val repository: FinanceRepository by lazy {
        FinanceRepository(
            householdDao = database.householdDao(),
            categoryDao = database.categoryDao(),
            incomeDao = database.incomeDao(),
            expenseDao = database.expenseDao(),
            database = database
        )
    }
}
