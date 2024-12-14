package com.example.expensetracker

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MyApp(recordViewModel: RecordViewModel, currencyViewModel: CurrencyViewModel) {
    val navController = rememberNavController()
    MainScaffold(navController, recordViewModel, currencyViewModel) // Use MainScaffold instead of NavGraph
}
