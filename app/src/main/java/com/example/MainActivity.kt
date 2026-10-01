package com.example
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.EmeraldBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val scope = rememberCoroutineScope()
                val database = AppDatabase.getDatabase(applicationContext, scope)
                val repository = FinanceRepository(
                    householdDao = database.householdDao(),
                    categoryDao = database.categoryDao(),
                    incomeDao = database.incomeDao(),
                    expenseDao = database.expenseDao(),
                    budgetDao = database.budgetDao()
                )

                val viewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(repository)
                )

                val currentScreen by viewModel.currentScreen.collectAsState()

                // Hardware / gesture back handling
                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    viewModel.navigateBack()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EmeraldBackground
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(250),
                        label = "screen_crossfade"
                    ) { screen ->
                        when (screen) {
                            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                            AppScreen.BUDGET -> BudgetScreen(viewModel = viewModel)
                            AppScreen.STATISTICS -> StatisticsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
