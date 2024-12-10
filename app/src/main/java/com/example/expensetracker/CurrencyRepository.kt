package com.example.expensetracker

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CurrencyRepository(private val currencyDao: CurrencyDao) {

    private val apiUrl = "https://openexchangerates.org/api/currencies.json"

    suspend fun fetchAndStoreCurrencies() {
        val count = currencyDao.getCount()
        if (count > 0) {
            Log.d("CurrencyRepository", "Currencies already exist in the database.")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }

                val currencies = parseCurrencies(response)
                currencyDao.insertAll(currencies)

                Log.d("CurrencyRepository", "Currencies fetched and stored successfully.")
            } catch (e: Exception) {
                Log.e("CurrencyRepository", "Error fetching currencies: ${e.message}")
            }
        }
    }

    private fun parseCurrencies(json: String): List<Currency> {
        val currencies = mutableListOf<Currency>()
        val jsonObject = JSONObject(json)
        jsonObject.keys().forEach { key ->
            val name = jsonObject.getString(key)
            currencies.add(Currency(code = key, name = name))
        }
        return currencies
    }

    // Fetch all currencies from the database
    suspend fun getAllCurrencies(): List<Currency> {
        return currencyDao.getAllCurrencies()
    }
}
