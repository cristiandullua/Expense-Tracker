package com.example.expensetracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModelFactory(
    private val context: Context,
    private val recordRepository: RecordRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(context, recordRepository, currencyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
