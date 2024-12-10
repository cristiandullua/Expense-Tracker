package com.example.expensetracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Record::class, Currency::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun currencyDao(): CurrencyDao
}
