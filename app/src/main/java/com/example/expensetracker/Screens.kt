@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen() {
    Text(text = "Home Screen")
}

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Expense",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = !isExpense,
                            onCheckedChange = { isExpense = !it }
                        )
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
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
                    RecordItem(record, navController)  // Pass ViewModel and NavController
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: Record, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Navigate to the EditRecordScreen when the record is clicked
                navController.navigate("editRecordScreen/${record.id}")
            },
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Center content vertically
        ) {
            // Left Column for category, date, and description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                // Category
                Text("Category: ${record.category}", style = MaterialTheme.typography.bodyMedium)
                // Date
                Text("Date: ${record.date}", style = MaterialTheme.typography.bodySmall)
                // Description
                Text("Description: ${record.description}", style = MaterialTheme.typography.bodySmall)
            }

            // Right Column for amount (larger font size)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End) // Aligns the amount to the right
            ) {
                Text(
                    text = "${record.amount}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp) // Adjust size as needed
                )
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
                TopAppBar(
                    title = { Text("Edit Record") },
                    actions = {
                        IconButton(onClick = {
                            viewModel.deleteRecord(currentRecord)
                            navController.popBackStack() // Navigate back after deletion
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Record"
                            )
                        }
                    }
                )
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

                // Save record button
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
                            navController.popBackStack() // Go back to the previous screen
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Save Changes")
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

@Composable
fun Header(title: String, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            DropdownMenuButton(onMenuClick)
        }
    )
}