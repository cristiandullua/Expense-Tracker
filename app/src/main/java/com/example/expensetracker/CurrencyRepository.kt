package com.example.expensetracker

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CurrencyRepository(private val currencyDao: CurrencyDao) {

    private val apiUrl = "https://openexchangerates.org/api/currencies.json"
    private val historicalApiUrl = "https://openexchangerates.org/api/historical/"  // For historical rates
    private val apiKey = "a3490f78e0174ea5bd4474f37027869d" // Your Open Exchange Rates API key

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

    // Fetch historical exchange rate for a specific date
    suspend fun getHistoricalRate(date: String, fromCurrency: String, toCurrency: String): Double? {
        val url = URL("$historicalApiUrl$date.json?app_id=$apiKey&base=$toCurrency&symbols=$fromCurrency")
        return withContext(Dispatchers.IO) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }

                val jsonResponse = JSONObject(response)
                val rates = jsonResponse.getJSONObject("rates")

                // Ensure the key exists and fetch the value as Double
                if (rates.has(fromCurrency)) {
                    rates.getDouble(fromCurrency)
                } else {
                    Log.e("CurrencyRepository", "Key $toCurrency not found in rates")
                    null
                }
            } catch (e: Exception) {
                Log.e("CurrencyRepository", "Error fetching historical rates: ${e.message}")
                null
            }
        }
    }
}
