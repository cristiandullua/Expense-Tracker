@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

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
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()

        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                MainScaffold(navController, recordViewModel, currencyViewModel, settingsViewModel)
            }
        }
    }

    private fun initViewModels() {
        // Get the database instance
        val database = AppDatabase.getDatabase(applicationContext)

        // Initialize the RecordRepository
        val recordRepository = RecordRepository(database.recordDao())
        recordViewModel = ViewModelProvider(this, RecordViewModelFactory(recordRepository))[RecordViewModel::class.java]

        // Initialize the CurrencyRepository
        val currencyRepository = CurrencyRepository(database.currencyDao())

        // Initialize the SettingsViewModel using the factory
        val settingsViewModelFactory = SettingsViewModelFactory(applicationContext, recordRepository, currencyRepository)
        settingsViewModel = ViewModelProvider(this, settingsViewModelFactory)[SettingsViewModel::class.java]

        // Initialize the CurrencyViewModel with its own factory
        val currencyViewModelFactory = CurrencyViewModelFactory(settingsViewModel, currencyRepository)
        currencyViewModel = ViewModelProvider(this, currencyViewModelFactory)[CurrencyViewModel::class.java].apply {
            initializeCurrencies() // Make sure this function initializes the data
        }
    }
}