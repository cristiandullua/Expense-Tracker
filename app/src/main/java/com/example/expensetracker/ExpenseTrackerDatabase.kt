// ExpenseTrackerDatabase.kt
package com.example.expensetracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class], version = 1)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseTrackerDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ExpenseTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseTrackerDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addCallback(ExpenseTrackerDatabaseCallback(scope))  // Add the callback here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Define the RoomDatabase.Callback inner class correctly
    private class ExpenseTrackerDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.transactionDao())
                }
            }
        }

        // Populate the database with default categories
        suspend fun populateDatabase(transactionDao: TransactionDao) {
            val defaultCategories = listOf(
                Category(name = "Food"),
                Category(name = "Transportation"),
                Category(name = "Entertainment"),
                Category(name = "Bills"),
                Category(name = "Shopping")
            )
            defaultCategories.forEach { transactionDao.insertCategory(it) }
        }
    }
}
