package com.example.expensetracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    // Insert a new record
    @Insert
    suspend fun insert(record: Record)

    // Fetch all records
    @Query("SELECT * FROM records")
    fun getAllRecords(): Flow<List<Record>> // Use Flow for reactive data

    // Update an existing record
    @Update
    suspend fun update(record: Record)

    // Delete a specific record
    @Delete
    suspend fun delete(record: Record)

    // Fetch a specific record by ID (for editing)
    @Query("SELECT * FROM records WHERE id = :recordId")
    fun getRecordByIdFlow(recordId: Int): Flow<Record?>
}
