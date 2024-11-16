package com.example.expensetracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { ExpenseTrackerDatabase.getDatabase(this, applicationScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyApp() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("Home", "Favorite", "Star")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Star)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.FavoriteBorder, Icons.Outlined.StarBorder)

    // Navigation Host
    NavHost(navController = navController, startDestination = "mainScreen") {
        // Main screen with navbar and FAB
        composable("mainScreen") {
            Scaffold(
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
                when (selectedItem) {
                    0 -> HomeScreen()
                    1 -> RecordsScreen()
                    2 -> StarScreen()
                }
            }
        }

        // FAB screen (the new screen without navbar)
        composable("fabScreen") {
            val database = Room.databaseBuilder(
                LocalContext.current,
                ExpenseTrackerDatabase::class.java, "expense_tracker_db"
            ).build()

            FabScreenPlaceholder(transactionDao = database.transactionDao())
        }
    }
}

@Composable
fun HomeScreen() {
    Text(text = "Home Screen")
}

@Composable
fun RecordsScreen() {
    Text(text = "Records Screen")
}

@Composable
fun StarScreen() {
    Text(text = "Star Screen")
}

// Placeholder screen for FAB action
@Composable
fun FabScreenPlaceholder(transactionDao: TransactionDao) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val scope = rememberCoroutineScope()

    // Load categories from the database
    val categories = remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(Unit) {
        categories.value = transactionDao.getCategories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = place,
            onValueChange = { place = it },
            label = { Text("Place") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Type:")
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isIncome = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Income")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isIncome = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Expense")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var isDropdownExpanded by remember { mutableStateOf(false) }

        // Display selected category or prompt text
        Text(
            text = selectedCategory?.name ?: "Select Category",
            modifier = Modifier
                .clickable { isDropdownExpanded = true }
        )
        // Category dropdown
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            categories.value.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) }, // Use text parameter here
                    onClick = {
                        selectedCategory = category
                        isDropdownExpanded = false // Close menu after selection
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val transaction = Transaction(
                    type = if (isIncome) "income" else "expense",
                    description = description,
                    amount = if (isIncome) amount.toDouble() else -amount.toDouble(),
                    place = place,
                    categoryId = selectedCategory?.id ?: 0
                )
                scope.launch {
                    transactionDao.insertTransaction(transaction)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Record")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}
