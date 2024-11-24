@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.room.Room

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "expense_tracker_db"
        ).build()

        val repository = RecordRepository(database.recordDao())
        val factory = RecordViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        setContent {
            MyApp(viewModel = viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyApp(viewModel: RecordViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("Home", "Records", "Star")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Payments, Icons.Filled.Star)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Payments, Icons.Outlined.StarBorder)

    // Navigation Host
    NavHost(navController = navController, startDestination = "mainScreen") {
        // Main screen with navbar and FAB
        composable("mainScreen") {
            Scaffold(
                topBar = {
                    if (selectedItem in listOf(0, 1, 2)) { // Show header only for Home, Records, and Star
                        Header(
                            title = when (selectedItem) {
                                0 -> "Home"
                                1 -> "Records"
                                2 -> "Star"
                                else -> ""
                            },
                            onMenuClick = { /* TODO: Handle menu actions */ }
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
            ) {
                // Display different screens based on selected item
                when (selectedItem) {
                    0 -> HomeScreen()
                    1 -> RecordsScreen(viewModel = viewModel, navController = navController) // Pass viewModel here
                    2 -> StarScreen()
                }
            }
        }

        // FAB screen (Create new record)
        composable("fabScreen") {
            CreateRecordScreen(navController = navController, viewModel = viewModel)
        }

        // Record screen (to display records from the db)
        composable("recordsScreen") {
            RecordsScreen(viewModel = viewModel, navController = navController) // Pass viewModel here as well
        }

        composable(
            "editRecordScreen/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId")
            recordId?.let {
                // Fetch the record as a Flow
                val recordFlow = viewModel.getRecordByIdFlow(it)

                // Ensure parameter names match the function signature
                EditRecordScreen(
                    navController = navController,
                    recordFlow = recordFlow, // Correct parameter name
                    viewModel = viewModel
                )
            } ?: run {
                // Handle null case (optional)
                Text("Invalid record ID.", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DropdownMenuButton(onMenuClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Option 1") },
            onClick = { onMenuClick() }
        )
        DropdownMenuItem(
            text = { Text("Option 2") },
            onClick = { onMenuClick() }
        )
        DropdownMenuItem(
            text = { Text("Option 3") },
            onClick = { onMenuClick() }
        )
    }
}


