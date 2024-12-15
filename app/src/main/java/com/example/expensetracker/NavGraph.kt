package com.example.expensetracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

sealed class Destinations(val route: String) {
    object Home : Destinations("home")
    object CreateRecord : Destinations("createRecord")
    object Records : Destinations("records")
    object Settings : Destinations("settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    recordViewModel: RecordViewModel,
    currencyViewModel: CurrencyViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Observe the current route using navController.currentBackStackEntryAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    // Explicitly define the type of navigationItems
    val navigationItems: List<Pair<Destinations, ImageVector>> = listOf(
        Destinations.Home to Icons.Default.Home,
        Destinations.Records to Icons.Default.Payments,
        Destinations.Settings to Icons.Default.Settings
    )

    // Dynamically determine the selected item based on current route
    val selectedItem = navigationItems.indexOfFirst { it.first.route == currentBackStackEntry?.destination?.route }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Dynamically change the title based on the current route
                    val currentRoute = currentBackStackEntry?.destination?.route
                    Text(
                        when {
                            currentRoute == Destinations.CreateRecord.route -> "Add Record"
                            currentRoute?.startsWith("editRecord") == true -> "Edit Record"
                            currentRoute == Destinations.Records.route -> "Records"
                            currentRoute == Destinations.Settings.route -> "Settings"
                            currentRoute == Destinations.Home.route -> "Home"
                            else -> "   " // Default title for other screens
                        }
                    )
                },
                actions = {
                    // Only show the delete action in the EditRecord screen
                    if (currentBackStackEntry?.destination?.route?.startsWith("editRecord") == true) {
                        val recordId = currentBackStackEntry!!.arguments?.getInt("recordId")
                        recordId?.let { id ->
                            val recordFlow = recordViewModel.getRecordByIdFlow(id)
                            val record by recordFlow.collectAsState(initial = null)

                            // Show delete icon if the record exists
                            record?.let { currentRecord ->
                                IconButton(onClick = {
                                    recordViewModel.deleteRecord(currentRecord)
                                    navController.popBackStack() // Navigate back after deletion
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Record"
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Destinations.CreateRecord.route) {
                        popUpTo(Destinations.CreateRecord.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, (destination, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = destination.route) },
                        label = { Text(destination.route.capitalize(java.util.Locale.ROOT)) },
                        selected = selectedItem == index,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(Destinations.CreateRecord.route) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Home.route) {
                HomeScreen(recordViewModel)
            }
            composable(Destinations.Records.route) {
                RecordsScreen(recordViewModel, settingsViewModel, navController)
            }
            composable(Destinations.Settings.route) {
                SettingsScreen(settingsViewModel, currencyViewModel)
            }
            composable(Destinations.CreateRecord.route) {
                CreateRecordScreen(navController, recordViewModel, currencyViewModel, settingsViewModel)
            }
            composable(
                "editRecordScreen/{recordId}",
                arguments = listOf(navArgument("recordId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getInt("recordId")

                recordId?.let {
                    val recordFlow = recordViewModel.getRecordByIdFlow(it)
                    EditRecordScreen(
                        navController = navController,
                        recordFlow = recordFlow,
                        viewModel = recordViewModel,
                        currencyViewModel = currencyViewModel,
                        settingsViewModel = settingsViewModel
                    )
                } ?: run {
                    Text("Invalid record ID", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}
