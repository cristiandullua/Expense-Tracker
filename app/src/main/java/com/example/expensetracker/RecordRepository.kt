package com.example.expensetracker

import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao: RecordDao) {

    // Returns a Flow of all records to be observed by the ViewModel
    fun getAllRecords(): Flow<List<Record>> = recordDao.getAllRecords()

    // Insert a new record into the database
    suspend fun insertRecord(record: Record) {
        recordDao.insert(record)
    }
}
