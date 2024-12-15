package com.example.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CurrencyViewModelFactory(
    private val settingsViewModel: SettingsViewModel,
    private val currencyRepository: CurrencyRepository,
    private val recordRepository: RecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencyViewModel(currencyRepository, recordRepository, settingsViewModel) as T
    }
}

