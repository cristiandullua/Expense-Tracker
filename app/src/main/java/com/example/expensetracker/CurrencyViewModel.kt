package com.example.expensetracker

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CurrencyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val recordRepository: RecordRepository,
    context: Context // Pass context to the ViewModel
) : ViewModel() {

    // Shared Preferences helper
    private val preferencesHelper = PreferencesHelper(context)

    // State to hold currencies
    private val _currencies = mutableStateOf<List<Currency>>(emptyList())
    val currencies: State<List<Currency>> get() = _currencies

    // Default base currency
    private val _baseCurrency = mutableStateOf(preferencesHelper.getBaseCurrency() ?: "USD")
    val baseCurrency: State<String> = _baseCurrency

    // State to hold whether to display in base currency
    private val _displayInBaseCurrency = mutableStateOf(preferencesHelper.getDisplayInBaseCurrency())
    val displayInBaseCurrency: State<Boolean> = _displayInBaseCurrency

    init {
        loadCurrenciesFromDatabase()
    }

    // Function to update the base currency
    fun setBaseCurrency(currency: String) {
        _baseCurrency.value = currency
        preferencesHelper.saveBaseCurrency(currency)  // Save the base currency
        updateConvertedAmounts()
    }

    // Function to update the displayInBaseCurrency state
    fun setDisplayInBaseCurrency(value: Boolean) {
        _displayInBaseCurrency.value = value
        preferencesHelper.saveDisplayInBaseCurrency(value)  // Save the display setting
        if (value) {
            updateConvertedAmounts()
        }
    }

    // Load currencies from the database
    private fun loadCurrenciesFromDatabase() {
        viewModelScope.launch {
            _currencies.value = currencyRepository.getAllCurrencies()
        }
    }

    // Initialize and fetch currencies
    fun initializeCurrencies() {
        viewModelScope.launch {
            currencyRepository.fetchAndStoreCurrencies()
            loadCurrenciesFromDatabase() // Reload after fetching
        }
    }

    // Update records with converted amounts
    private fun updateConvertedAmounts() {
        viewModelScope.launch {
            val baseCurrencyCode = _baseCurrency.value
            recordRepository.getAllRecords().first().forEach { record ->
                val rate = currencyRepository.getHistoricalRate(record.date, record.currency, baseCurrencyCode)
                if (rate != null) {
                    val convertedAmount = (record.amount / rate).toBigDecimal()
                        .setScale(2, java.math.RoundingMode.HALF_EVEN)
                        .toDouble()
                    val updatedRecord = record.copy(convertedAmount = convertedAmount)
                    recordRepository.update(updatedRecord)
                }
            }
        }
    }

    suspend fun fetchAndConvertAmount(record: Record): Double? {
        val baseCurrencyCode = _baseCurrency.value
        val rate = currencyRepository.getHistoricalRate(record.date, record.currency, baseCurrencyCode)
        return rate?.let { (record.amount / it).toBigDecimal().setScale(2, java.math.RoundingMode.HALF_EVEN).toDouble() }
    }
}
