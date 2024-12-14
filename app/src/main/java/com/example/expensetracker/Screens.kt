@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensetracker

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(recordViewModel: RecordViewModel) {
    val currentMonth = remember { mutableStateOf(1) }
    val negativeExpenses by recordViewModel.getNegativeExpensesGroupedByCurrencyAndMonth(currentMonth.value).collectAsState(initial = emptyMap())
    val categorySpending by recordViewModel.getNegativeSpendingGroupedByCategory(currentMonth.value).collectAsState(initial = emptyMap())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(70.dp)) // Add space for top bar visibility

        // Spending per Currency Title
        Text(
            text = "Spending per Currency",
            style = MaterialTheme.typography.headlineSmall
        )

        // Month selector dropdown with title in the same row
        MonthSelector(currentMonth = currentMonth)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (negativeExpenses.isEmpty()) {
                    Text(
                        text = "No expenses recorded for this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    negativeExpenses.forEach { (currency, totalAmount) ->
                        CurrencyCard(currency = currency, totalAmount = totalAmount)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

        // Spending per Category Title
        Text(
            text = "Spending per Category",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Unified Surface for the Pie Chart and Category List
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (categorySpending.isEmpty()) {
                    // Message if no records are available
                    Text(
                        text = "No categories available for this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    // Display Pie Chart
                    SpendingPieChart(categories = categorySpending, totalAmount = categorySpending.values.sum())

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display Category List
                    SpendingCategoryList(categories = categorySpending, totalAmount = categorySpending.values.sum())
                }
            }
        }

        Spacer(modifier = Modifier.height(150.dp)) // Bottom padding
    }
}


@Composable
fun SpendingPieChart(categories: Map<String, Double>, totalAmount: Double) {
    val categoryColors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta,
        Color.Cyan, Color.Gray, Color.LightGray, Color.DarkGray, Color.Black
    )

    val categoryEntries = categories.entries.toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the container square
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = 0f
            categoryEntries.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / totalAmount) * 360f
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle.toFloat(),
                    useCenter = true
                )
                startAngle += sweepAngle.toFloat()
            }
        }
    }
}

@Composable
fun SpendingCategoryList(categories: Map<String, Double>, totalAmount: Double) {
    val categoryColors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta,
        Color.Cyan, Color.Gray, Color.LightGray, Color.DarkGray, Color.Black
    )

    // Sort categories by value in descending order
    val sortedCategories = categories.entries.sortedBy { it.value }

    Column(modifier = Modifier.fillMaxWidth()) {
        sortedCategories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowCategories.forEachIndexed { index, entry ->
                    Card(
                        modifier = Modifier
                            .weight(1f) // Ensures even spacing between two cards
                            .padding(4.dp), // Reduced padding between cards
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.elevatedCardElevation()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp) // Minimal padding inside the card
                        ) {
                            // Color indicator circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = categoryColors[sortedCategories.indexOf(entry) % categoryColors.size],
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Category details
                            Column {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${(entry.value / totalAmount * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Add an invisible spacer card to fill the space if there's only one card in the last row
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                }
            }
        }
    }
}

@Composable
fun MonthSelector(currentMonth: MutableState<Int>) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val expanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Padding to give space around
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Month",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .weight(1f) // Make it take the remaining space
                .clickable { expanded.value = !expanded.value } // Make the entire box clickable
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // Border color
                    shape = MaterialTheme.shapes.small // Rounded corners
                )
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)) // Light background
        ) {
            Text(
                text = months[currentMonth.value - 1], // Display the selected month
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(
                        text = { Text(text = month) },
                        onClick = {
                            currentMonth.value = index + 1
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyCard(currency: String, totalAmount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currency, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "$${"%.2f".format(totalAmount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (totalAmount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsScreen(currencyViewModel: CurrencyViewModel) {
    var currencyExpanded by remember { mutableStateOf(false) }
    val displayInBaseCurrency by currencyViewModel.displayInBaseCurrency
    val currencies by currencyViewModel.currencies
    val baseCurrencyCode by currencyViewModel.baseCurrency
    val baseCurrencyName = currencies.find { it.code == baseCurrencyCode }?.name ?: "Unknown"

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Currency Dropdown
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = !currencyExpanded },
                modifier = Modifier.fillMaxWidth() // Ensure full-width dropdown
            ) {
                TextField(
                    value = baseCurrencyCode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Currency") }, // Updated label
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, // Anchors the menu to the bottom of the TextField
                            enabled = true // Enable the anchor functionality
                        )
                        .fillMaxWidth() // Ensure consistent width
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    modifier = Modifier.fillMaxWidth() // Ensure consistent width
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text("${currency.code} - ${currency.name}") },
                            onClick = {
                                currencyViewModel.setBaseCurrency(currency.code) // Triggering the update
                                currencyExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth() // Ensure consistent width
                        )
                    }
                }
            }

// Display selected currency name below dropdown
            Text(
                text = buildAnnotatedString {
                    append("Selected Currency: ")
                    // Apply bold only to the currency name
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(baseCurrencyName) // Make only the currency name bold
                    }
                },
                style = MaterialTheme.typography.bodyMedium // Apply the default body style for the entire text
            )


            // Divider between sections
            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            // Toggle for Displaying Records in Base Currency
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Display Records in Base Currency",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
// In your SettingsScreen composable, inside the Switch onCheckedChange:
                Switch(
                    checked = displayInBaseCurrency,
                    onCheckedChange = { newState ->
                        currencyViewModel.setDisplayInBaseCurrency(newState) // Triggering the update
                    }
                )
            }

            // Short explanation for the switch
            Text(
                text = "When active, all amounts will be displayed in the base currency.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
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

private fun convertMillisToDatabaseFormat(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Set the time zone to UTC to avoid issues with local time zone offsets
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

fun convertDateToTimestamp(dateString: String): Long {
    // Parse the string (YYYY-MM-DD) into a LocalDate and convert to timestamp (milliseconds)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(dateString)
    return date?.time ?: 0L
}

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
    var isSaving by remember { mutableStateOf(false) } // State to track saving process
    var convertedAmount by remember { mutableStateOf<Double?>(null) } // State to hold converted amount

    val categories = listOf(
        "Food",
        "Transport",
        "Rent",
        "Entertainment",
        "Utilities",
        "Healthcare",
        "Shopping",
        "Savings",
        "Travel",
        "Income",
        "Others"
    )

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
                            modifier = Modifier.menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, // Anchors the menu to the bottom of the TextField
                                enabled = true // Enable the anchor functionality
                            )
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
                            onCheckedChange = { checked -> isIncome = checked }
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
                        modifier = Modifier.menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, // Anchors the menu to the bottom of the TextField
                            enabled = true // Enable the anchor functionality
                        )
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
                        isSaving = true // Trigger state change to start coroutine work
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving // Disable button during saving process
                ) {
                    Text(text = if (isSaving) "Saving..." else "Save Record")
                }
            }
        }
    }
    // Perform coroutine work when isSaving becomes true
    if (isSaving) {
        LaunchedEffect(isSaving) {
            try {
                val recordAmount = amount.toDoubleOrNull() ?: 0.0
                val record = Record(
                    amount = if (isIncome) recordAmount else -recordAmount, // Convert based on switch
                    date = convertMillisToDatabaseFormat(dateTimestamp), // Store date in YYYY-MM-DD format
                    category = selectedCategory,
                    description = description,
                    currency = selectedCurrency // Save currency code
                )

                val baseCurrencyCode = currencyViewModel.baseCurrency.value
                val result = baseCurrencyCode.let {
                    currencyViewModel.fetchAndConvertAmount(record) // Suspend function
                }

                convertedAmount = result // Update state with converted amount

                // Save record with the converted amount
                val recordWithConversion = record.copy(convertedAmount = result ?: 0.0)
                recordViewModel.saveRecord(recordWithConversion)

                // Navigate back
                navController.popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSaving = false // Reset saving state
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
        var dateDisplay by remember { mutableStateOf(convertMillisToDayAndDateUTC(convertDateToTimestamp(currentRecord.date))) } // For display
        var dateTimestamp by remember { mutableLongStateOf(convertDateToTimestamp(currentRecord.date)) } // For database storage
        var selectedCategory by remember { mutableStateOf(currentRecord.category) }
        var description by remember { mutableStateOf(currentRecord.description) }
        var expanded by remember { mutableStateOf(false) }
        var selectedCurrency by remember { mutableStateOf(currentRecord.currency) }
        var currencyExpanded by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        val categories = listOf(
            "Food",
            "Transport",
            "Rent",
            "Entertainment",
            "Utilities",
            "Healthcare",
            "Shopping",
            "Savings",
            "Travel",
            "Income",
            "Others"
        )
        val currencies by currencyViewModel.currencies

        // Determine the switch state based on the amount
        var isIncome by remember { mutableStateOf(currentRecord.amount > 0) }
        var isSaving by remember { mutableStateOf(false) }

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
                                modifier = Modifier.menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, // Anchors the menu to the bottom of the TextField
                                    enabled = true // Enable the anchor functionality
                                )
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
                            modifier = Modifier.menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, // Anchors the menu to the bottom of the TextField
                                enabled = true // Enable the anchor functionality
                            )
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
                            isSaving = true // Trigger saving process
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving // Disable button during saving process
                    ) {
                        Text(text = if (isSaving) "Saving..." else "Save Changes")
                    }
                }
            }
            // Perform coroutine work when isSaving becomes true
            if (isSaving) {
                LaunchedEffect(isSaving) {
                    try {
                        val recordAmount = amount.toDoubleOrNull() ?: 0.0
                        val updatedRecord = currentRecord.copy(
                            amount = if (isIncome) recordAmount else -recordAmount, // Convert based on switch
                            date = convertMillisToDatabaseFormat(dateTimestamp), // Store date in YYYY-MM-DD format
                            category = selectedCategory,
                            description = description,
                            currency = selectedCurrency // Save currency code
                        )

                        val baseCurrencyCode = currencyViewModel.baseCurrency.value
                        val result = baseCurrencyCode.let {
                            currencyViewModel.fetchAndConvertAmount(updatedRecord) // Suspend function
                        }

                        val recordWithConversion = updatedRecord.copy(convertedAmount = result ?: 0.0)
                        viewModel.updateRecord(recordWithConversion)

                        // Navigate back after successful save
                        navController.popBackStack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isSaving = false // Reset saving state
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
fun RecordsScreen(
    recordViewModel: RecordViewModel,
    currencyViewModel: CurrencyViewModel,
    navController: NavController
) {
    val records by recordViewModel.allRecords.collectAsState(initial = emptyList())
    val baseCurrency by currencyViewModel.baseCurrency // Base currency state
    val displayInBaseCurrency by currencyViewModel.displayInBaseCurrency // Display flag

    // Sort records by date and ID in descending order
    val sortedRecords = records.sortedWith(
        compareByDescending<Record> { it.date } // Replace `date` with the actual property name for the date
            .thenByDescending { it.id } // Replace `id` with the actual property name for the ID
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Records") })
        }
    ) { innerPadding ->
        if (sortedRecords.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 150.dp) // Add extra bottom padding for safety
            ) {
                items(sortedRecords) { record ->
                    RecordItem(
                        record = record,
                        navController = navController,
                        displayInBaseCurrency = displayInBaseCurrency,
                        baseCurrencySymbol = baseCurrency
                    )
                }
            }
        }
    }
}

@Composable
fun RecordItem(
    record: Record,
    navController: NavController,
    displayInBaseCurrency: Boolean,
    baseCurrencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
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
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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
                // Main displayed amount and currency
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val amountToDisplay = if (displayInBaseCurrency) record.convertedAmount else record.amount
                    val currencyToDisplay = if (displayInBaseCurrency) baseCurrencySymbol else record.currency

                    Text(
                        text = "$amountToDisplay",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currencyToDisplay,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Original amount and currency when displaying in base currency
                if (displayInBaseCurrency) {
                    Spacer(modifier = Modifier.height(4.dp)) // Small spacing
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${record.amount}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) // Slightly dimmed color
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.currency,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) // Slightly dimmed color
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Header(title: String) {
    TopAppBar(
        title = { Text(title) },
        actions = {

        }
    )
}