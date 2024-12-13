package com.example.expensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val date: String,
    val category: String,
    val description: String,
    val currency: String = "USD",
    val convertedAmount: Double = 0.0 // Default to 0.0
)