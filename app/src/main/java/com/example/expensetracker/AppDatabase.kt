package com.example.expensetracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}
