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

    init {
        loadCurrenciesFromDatabase()
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

