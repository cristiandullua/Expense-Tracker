package com.example.expensetracker

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // State to hold currencies
    private val _currencies = mutableStateOf<List<Currency>>(emptyList())
    val currencies: State<List<Currency>> get() = _currencies

    // Default base currency
    private val _baseCurrency = mutableStateOf("USD")
    val baseCurrency: State<String> = _baseCurrency

    // State to hold whether to display in base currency
    private val _displayInBaseCurrency = mutableStateOf(false)
    val displayInBaseCurrency: State<Boolean> get() = _displayInBaseCurrency

    init {
        loadCurrenciesFromDatabase()
    }

    // Function to update the base currency
    fun setBaseCurrency(currency: String) {
        _baseCurrency.value = currency
    }

    // Function to update the displayInBaseCurrency state
    fun setDisplayInBaseCurrency(value: Boolean) {
        _displayInBaseCurrency.value = value
    }

    // Load currencies from the database
    private fun loadCurrenciesFromDatabase() {
        viewModelScope.launch {
            _currencies.value = repository.getAllCurrencies()
        }
    }

    fun initializeCurrencies() {
        viewModelScope.launch {
            repository.fetchAndStoreCurrencies()
            loadCurrenciesFromDatabase() // Reload after fetching
        }
    }

    fun getCurrencyByCode(code: String): Currency? {
        return _currencies.value.find { it.code == code }
    }
}
