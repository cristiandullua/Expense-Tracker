package com.example.expensetracker

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel(context: Context) : ViewModel() {

    // Shared Preferences helper
    private val preferencesHelper = PreferencesHelper(context)

    // State to hold the base currency
    private val _baseCurrency = mutableStateOf(preferencesHelper.getBaseCurrency() ?: "USD")
    val baseCurrency: State<String> = _baseCurrency

    // State to hold whether to display in base currency
    private val _displayInBaseCurrency = mutableStateOf(preferencesHelper.getDisplayInBaseCurrency())
    val displayInBaseCurrency: State<Boolean> = _displayInBaseCurrency

    // Function to update the base currency
    fun setBaseCurrency(currency: String) {
        _baseCurrency.value = currency
        preferencesHelper.saveBaseCurrency(currency)  // Save the base currency
    }

    // Function to update the displayInBaseCurrency state
    fun setDisplayInBaseCurrency(value: Boolean) {
        _displayInBaseCurrency.value = value
        preferencesHelper.saveDisplayInBaseCurrency(value)  // Save the display setting
    }
}
