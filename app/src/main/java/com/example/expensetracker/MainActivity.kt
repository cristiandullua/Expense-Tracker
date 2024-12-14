@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var currencyViewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()

        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                MainScaffold(navController, recordViewModel, currencyViewModel)
            }
        }
    }

    private fun initViewModels() {
        val database = AppDatabase.getDatabase(applicationContext)
        val recordRepository = RecordRepository(database.recordDao())
        recordViewModel = ViewModelProvider(this, RecordViewModelFactory(recordRepository))[RecordViewModel::class.java]

        // Get the context from the current Composable
        val context: Context = applicationContext

        val currencyRepository = CurrencyRepository(database.currencyDao())
        val factory = CurrencyViewModelFactory(context, currencyRepository, recordRepository)

        currencyViewModel = ViewModelProvider(this, factory)[CurrencyViewModel::class.java].apply {
            initializeCurrencies()
        }
    }
}