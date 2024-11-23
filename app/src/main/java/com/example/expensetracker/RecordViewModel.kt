package com.example.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    // Expose a Flow of records as LiveData or StateFlow for Compose
    val allRecords: StateFlow<List<Record>> = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to save a record
    fun saveRecord(record: Record) {
        viewModelScope.launch {
            repository.insertRecord(record)
        }
    }

    // Function to update an existing record
    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
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
