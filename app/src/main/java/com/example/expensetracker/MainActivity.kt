@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room database
        val database = AppDatabase.getDatabase(applicationContext)

        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        val currencyRepository = CurrencyRepository(database.currencyDao())
        val currencyViewModel = CurrencyViewModel(currencyRepository, repository)
        currencyViewModel.initializeCurrencies()

        setContent {
            MyApp(recordViewModel = recordViewModel, currencyViewModel = currencyViewModel)
        }
    }
}

@Composable
fun MyApp(recordViewModel: RecordViewModel, currencyViewModel: CurrencyViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("Home", "Records", "Settings")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Payments, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Payments, Icons.Outlined.Settings)

    // Navigation Host
    NavHost(navController = navController, startDestination = "mainScreen") {
        // Main screen with navbar and FAB
        composable("mainScreen") {
            Scaffold(
                topBar = {
                    if (selectedItem in listOf(0, 1, 2)) { // Show header only for Home, Records, and Settings
                        Header(
                            title = when (selectedItem) {
                                0 -> "Home"
                                1 -> "Records"
                                2 -> "Settings"
                                else -> ""
                            }
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            // Navigate to new screen when FAB is clicked
                            navController.navigate("fabScreen")
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                },
                bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                        contentDescription = item
                                    )
                                },
                                label = { Text(item) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index }
                            )
                        }
                    }
                }
            ) { paddingValues -> // Use paddingValues here
                // Display different screens based on selected item
                when (selectedItem) {
                    0 -> HomeScreen()
                    1 -> RecordsScreen(recordViewModel = recordViewModel, currencyViewModel = currencyViewModel, navController = navController) // Pass viewModel here
                    2 -> SettingsScreen(currencyViewModel = currencyViewModel) // Pass navController for consistency
                }
                // Apply the content padding to your screens
                Modifier.padding(paddingValues)
            }
        }

        // FAB screen (Create new record)
        composable("fabScreen") {
            CreateRecordScreen(navController = navController, recordViewModel = recordViewModel, currencyViewModel = currencyViewModel)
        }

        // Record screen (to display records from the db)
        composable("recordsScreen") {
            RecordsScreen(recordViewModel = recordViewModel, currencyViewModel = currencyViewModel, navController = navController) // Pass viewModel here as well
        }

        // Settings screen (newly added)
        composable("settingsScreen") {
            SettingsScreen(currencyViewModel)
        }

        composable(
            "editRecordScreen/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId")
            recordId?.let {
                // Fetch the record as a Flow
                val recordFlow = recordViewModel.getRecordByIdFlow(it)

                EditRecordScreen(
                    navController = navController,
                    recordFlow = recordFlow, // Correct parameter name
                    viewModel = recordViewModel,
                    currencyViewModel = currencyViewModel
                )
            } ?: run {
                // Handle null case (optional)
                Text("Invalid record ID.", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
            }
        }
    }
}


