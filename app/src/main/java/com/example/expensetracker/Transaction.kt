package com.example.expensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "income" or "expense"
    val description: String,
    val amount: Double,
    val place: String,
    val categoryId: Int
)

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
