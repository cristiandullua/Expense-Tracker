package com.example.expensetracker

import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao: RecordDao) {

    // Returns a Flow of all records to be observed by the ViewModel
    fun getAllRecords(): Flow<List<Record>> = recordDao.getAllRecords()

    // Insert a new record into the database
    suspend fun insertRecord(record: Record) {
        recordDao.insert(record)
    }

    // Update an existing record
    suspend fun updateRecord(record: Record) {
        recordDao.update(record)
    }

    // Delete a specific record
    suspend fun deleteRecord(record: Record) {
        recordDao.delete(record)
    }

    // Fetch a record by its ID
    fun getRecordByIdFlow(recordId: Int): Flow<Record?> {
        return recordDao.getRecordByIdFlow(recordId)
    }
}
