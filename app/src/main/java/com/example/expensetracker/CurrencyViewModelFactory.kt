package com.example.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CurrencyViewModelFactory(
    private val settingsViewModel: SettingsViewModel,
    private val currencyRepository: CurrencyRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrencyViewModel(currencyRepository, settingsViewModel) as T
    }
}

