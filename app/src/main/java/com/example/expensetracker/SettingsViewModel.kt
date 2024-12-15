package com.example.expensetracker

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.RoundingMode

class SettingsViewModel(
    context: Context,
    private val recordRepository: RecordRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

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

        // Trigger conversion when the setting is turned on
        if (value) {
            updateConvertedAmounts()
        }
    }

    // Update records with converted amounts
    private fun updateConvertedAmounts() {
        viewModelScope.launch {
            val baseCurrencyCode = _baseCurrency.value
            val records = recordRepository.getAllRecords().first() // Fetch all records
            records.forEach { record ->
                // Get historical rate for each record
                val rate = currencyRepository.getHistoricalRate(record.date, record.currency, baseCurrencyCode)
                if (rate != null) {
                    val convertedAmount = (record.amount / rate).toBigDecimal()
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .toDouble()
                    val updatedRecord = record.copy(convertedAmount = convertedAmount)
                    recordRepository.update(updatedRecord) // Update the record with converted amount
                }
            }
        }
    }
}
