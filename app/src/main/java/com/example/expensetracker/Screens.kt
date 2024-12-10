@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

@Composable
fun HomeScreen() {
    Text(text = "Home Screen")
}

@Composable
fun StarScreen() {
    Text(text = "Star Screen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (Long) -> Unit, // Return timestamp instead of string
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= System.currentTimeMillis()
        }
    })

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null) {
                    onDateSelected(selectedMillis) // Pass raw timestamp
                }
                onDismiss()
            }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun convertMillisToDayAndDateUTC(millis: Long): String {
    val formatter = SimpleDateFormat("EEEE - MMM dd, yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC") // Set the time zone to UTC
    return formatter.format(Date(millis))
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateRecordScreen(
    navController: NavController,
    recordViewModel: RecordViewModel,
    currencyViewModel: CurrencyViewModel
) {
    var amount by remember { mutableStateOf("") }
    var dateDisplay by remember { mutableStateOf("Pick a date") }
    var dateTimestamp by remember { mutableLongStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("USD") } // Default to USD
    var currencyExpanded by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(false) } // Track if it's an expense or income
    val categories = listOf("Food", "Transport", "Rent", "Entertainment")

    // Get the list of currencies from the view model
    val currencies by currencyViewModel.currencies

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Record") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between fields
                ) {
                    // Amount Field
                    TextField(
                        value = amount,
                        onValueChange = {
                            // Allow only valid numeric input
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                amount = it
                            }
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(2f) // Adjust weight to control width proportion
                    )

                    // Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false },
                            modifier = Modifier.width(300.dp) // Custom width for dropdown
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text("${currency.code} - ${currency.name}") },
                                    onClick = {
                                        selectedCurrency = currency.code
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(contentAlignment = Alignment.Center) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showDatePicker = true }
                    ) {
                        Text(text = dateDisplay)
                    }
                }

                if (showDatePicker) {
                    MyDatePickerDialog(
                        onDateSelected = { timestamp ->
                            dateTimestamp = timestamp
                            dateDisplay = convertMillisToDayAndDateUTC(timestamp)
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
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
                            checked = isIncome,
                            onCheckedChange = { checked ->
                                isIncome = checked
                            }
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
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    TextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
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
                        val recordAmount = amount.toDoubleOrNull() ?: 0.0

                        // Save the record to the database
                        val record = Record(
                            amount = if (isIncome) recordAmount else -recordAmount, // Convert based on switch
                            date = dateTimestamp,
                            category = selectedCategory,
                            description = description,
                            currency = selectedCurrency // Save currency code
                        )
                        recordViewModel.saveRecord(record)

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditRecordScreen(
    navController: NavController,
    recordFlow: Flow<Record?>,
    viewModel: RecordViewModel,
    currencyViewModel: CurrencyViewModel
) {
    // Collect the record from the Flow
    val record by recordFlow.collectAsState(initial = null)

    record?.let { currentRecord ->
        // State holders for form fields
        var amount by remember { mutableStateOf(currentRecord.amount.absoluteValue.toString()) } // Show absolute value
        var dateDisplay by remember { mutableStateOf(convertMillisToDayAndDateUTC(currentRecord.date.toLong())) } // For display
        var dateTimestamp by remember { mutableLongStateOf(currentRecord.date) } // For database storage
        var selectedCategory by remember { mutableStateOf(currentRecord.category) }
        var description by remember { mutableStateOf(currentRecord.description) }
        var expanded by remember { mutableStateOf(false) }
        var selectedCurrency by remember { mutableStateOf(currentRecord.currency) }
        var currencyExpanded by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        val categories = listOf("Food", "Transport", "Rent", "Entertainment")
        // Get the list of currencies from the view model
        val currencies by currencyViewModel.currencies

        // Determine the switch state based on the amount
        var isIncome by remember { mutableStateOf(currentRecord.amount > 0) }

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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between fields
                    ) {
                        // Amount Field
                        TextField(
                            value = amount,
                            onValueChange = {
                                // Allow only valid numeric input
                                if (it.all { char -> char.isDigit() || char == '.' }) {
                                    amount = it
                                }
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(2f) // Adjust weight to control width proportion
                        )

                        // Currency Dropdown
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = !currencyExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            TextField(
                                value = selectedCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                                },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                                modifier = Modifier.width(300.dp) // Custom width for dropdown
                            ) {
                                currencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text("${currency.code} - ${currency.name}") },
                                        onClick = {
                                            selectedCurrency = currency.code
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Box(contentAlignment = Alignment.Center) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showDatePicker = true }
                        ) {
                            Text(text = dateDisplay) // Display the formatted date
                        }
                    }

                    if (showDatePicker) {
                        MyDatePickerDialog(
                            onDateSelected = { timestamp ->
                                dateTimestamp = timestamp // Store timestamp for DB
                                dateDisplay = convertMillisToDayAndDateUTC(timestamp) // Format for display
                            },
                            onDismiss = { showDatePicker = false }
                        )
                    }
                }

                // Type toggle
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
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
                                checked = isIncome,
                                onCheckedChange = { checked ->
                                    isIncome = checked
                                }
                            )
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
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
                    val recordAmount = amount.toDoubleOrNull() ?: 0.0

                    Button(
                        onClick = {
                            val updatedRecord = currentRecord.copy(
                                amount = if (isIncome) recordAmount else -recordAmount, // Convert based on switch
                                date = dateTimestamp,
                                category = selectedCategory,
                                description = description,
                                currency = selectedCurrency // Save currency code
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
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column for category and description
            Column(
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 16.dp)
            ) {
                // Category
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                // Description
                if (record.description.isNotEmpty()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Right Column for amount and currency
            Column(
                modifier = Modifier
                    .weight(2f)
                    .wrapContentWidth(Alignment.End),
                horizontalAlignment = Alignment.End
            ) {
                // Row to display amount and currency side by side
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Amount with bold style
                    Text(
                        text = "${record.amount}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    // Space between amount and currency
                    Spacer(modifier = Modifier.width(4.dp))
                    // Currency with light style
                    Text(
                        text = record.currency,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
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