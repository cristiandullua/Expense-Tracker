@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.room.Room
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.Flow

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
        viewModel = ViewModelProvider(this, factory).get(RecordViewModel::class.java)

        setContent {
            MyApp(viewModel = viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyApp(viewModel: RecordViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

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
fun HomeScreen() {
    Text(text = "Home Screen")
}

//@Composable
//fun RecordsScreen() {
//    Text(text = "Records Screen")
//}

@Composable
fun StarScreen() {
    Text(text = "Star Screen")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateRecordScreen(navController: NavController, viewModel: RecordViewModel) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var isExpense by remember { mutableStateOf(true) }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Transport", "Rent", "Entertainment")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Record") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Ensures content is not obscured by the top bar
                .padding(16.dp),       // Adds padding around the screen content
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Type:")
                    Row {
                        Text(text = "Expense")
                        Switch(
                            checked = !isExpense,
                            onCheckedChange = { isExpense = !it }
                        )
                        Text(text = "Income")
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        // Save the record to the database
                        val record = Record(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = date,
                            isExpense = isExpense,
                            category = selectedCategory,
                            description = description
                        )
                        viewModel.saveRecord(record)

                        // Navigate back to the previous screen
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Save Record")
                }
            }
        }
    }
}

@Composable
fun RecordsScreen(viewModel: RecordViewModel, navController: NavController) {
    // Collecting records as a state
    val records by viewModel.allRecords.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Records") })
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No records found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    RecordItem(record, viewModel, navController)  // Pass ViewModel and NavController
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: Record, viewModel: RecordViewModel, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Category: ${record.category}", style = MaterialTheme.typography.titleMedium)
            Text("Amount: ${record.amount}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${record.date}", style = MaterialTheme.typography.bodySmall)
            Text("Description: ${record.description}", style = MaterialTheme.typography.bodySmall)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Edit button
                IconButton(onClick = {
                    navController.navigate("editRecordScreen/${record.id}") // Navigate to edit screen
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }

                // Delete button
                IconButton(onClick = {
                    viewModel.deleteRecord(record) // Call ViewModel to delete record
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditRecordScreen(
    navController: NavController,
    recordFlow: Flow<Record?>,
    viewModel: RecordViewModel
) {
    // Collect the record from the Flow
    val record by recordFlow.collectAsState(initial = null)

    record?.let { currentRecord ->
        // State holders for form fields
        var amount by remember { mutableStateOf(currentRecord.amount.toString()) }
        var date by remember { mutableStateOf(currentRecord.date) }
        var selectedCategory by remember { mutableStateOf(currentRecord.category) }
        var isExpense by remember { mutableStateOf(currentRecord.isExpense) }
        var description by remember { mutableStateOf(currentRecord.description) }
        var expanded by remember { mutableStateOf(false) }

        val categories = listOf("Food", "Transport", "Rent", "Entertainment")

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Edit Record") })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount field
                item {
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = amount.toDoubleOrNull() == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Date field
                item {
                    TextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Type toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Type:")
                        Row {
                            Text(text = "Expense")
                            Switch(
                                checked = !isExpense,
                                onCheckedChange = { isExpense = !it }
                            )
                            Text(text = "Income")
                        }
                    }
                }

                // Category dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Description field
                item {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Update button
                item {
                    Button(
                        onClick = {
                            val updatedRecord = currentRecord.copy(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                date = date,
                                isExpense = isExpense,
                                category = selectedCategory,
                                description = description
                            )
                            viewModel.updateRecord(updatedRecord)
                            navController.popBackStack() // Navigate back
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Update Record")
                    }
                }
            }
        }
    } ?: run {
        // Show loading state while the record is being fetched
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

