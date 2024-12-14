package com.example.expensetracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CurrencyViewModelFactory(
    private val context: Context,
    private val currencyRepository: CurrencyRepository,
    private val recordRepository: RecordRepository
) : ViewModelProvider.Factory {
    // Correcting the method signature
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencyViewModel(currencyRepository, recordRepository, context) as T
    }
}
