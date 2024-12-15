package com.example.expensetracker

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CurrencyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val settingsViewModel: SettingsViewModel // Inject SettingsViewModel here
) : ViewModel() {

    // State to hold currencies
    private val _currencies = mutableStateOf<List<Currency>>(emptyList())
    val currencies: State<List<Currency>> get() = _currencies

    init {
        loadCurrenciesFromDatabase()
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

    suspend fun fetchAndConvertAmount(record: Record): Double? {
        val baseCurrencyCode = settingsViewModel.baseCurrency.value
        val rate = currencyRepository.getHistoricalRate(record.date, record.currency, baseCurrencyCode)
        return rate?.let { (record.amount / it).toBigDecimal().setScale(2, java.math.RoundingMode.HALF_EVEN).toDouble() }
    }
}
