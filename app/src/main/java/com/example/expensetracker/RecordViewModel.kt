package com.example.expensetracker

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.State

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    // Expose a Flow of records as LiveData or StateFlow for Compose
    val allRecords: StateFlow<List<Record>> = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to get negative expenses grouped by currency and filtered by month
    fun getNegativeExpensesGroupedByCurrencyAndMonth(month: Int): Flow<Map<String, Double>> {
        return repository.getAllRecords().map { records ->
            records.filter { record ->
                // Parse the date string to LocalDate and check if the record is negative and matches the month
                val date = LocalDate.parse(record.date, DateTimeFormatter.ISO_DATE) // Parse string to LocalDate
                record.amount < 0 && date.monthValue == month // Check if the expense is negative and the month matches
            }
                .groupBy { it.currency } // Group by currency
                .mapValues { entry ->
                    entry.value.sumOf { it.amount } // Sum negative values per currency
                }
        }
    }

    fun getNegativeSpendingGroupedByCategory(month: Int): Flow<Map<String, Double>> {
        return repository.getAllRecords().map { records ->
            records.filter { record ->
                val date = LocalDate.parse(record.date, DateTimeFormatter.ISO_DATE)
                record.amount < 0 && date.monthValue == month
            }
                .groupBy { it.category }  // Group by category
                .mapValues { entry ->
                    entry.value.sumOf { it.convertedAmount } // Sum the convertedAmount per category
                }
        }
    }

    // Function to save a record
    fun saveRecord(record: Record) {
        viewModelScope.launch {
            repository.insertRecord(record)
        }
    }

    // Function to update an existing record
    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.update(record)
        }
    }

    // Function to delete a specific record
    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    // Function to get a specific record by ID (for editing)
    // Get a specific record by ID as a Flow
    fun getRecordByIdFlow(recordId: Int): Flow<Record?> {
        return repository.getRecordByIdFlow(recordId)
    }
}
