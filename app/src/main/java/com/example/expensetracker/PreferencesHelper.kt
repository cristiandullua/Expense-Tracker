package com.example.expensetracker

import android.content.Context

class PreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("currency_preferences", Context.MODE_PRIVATE)

    fun saveBaseCurrency(baseCurrencyCode: String) {
        sharedPreferences.edit().putString("baseCurrency", baseCurrencyCode).apply()
    }

    fun getBaseCurrency(): String? {
        return sharedPreferences.getString("baseCurrency", null)
    }

    fun saveDisplayInBaseCurrency(displayInBaseCurrency: Boolean) {
        sharedPreferences.edit().putBoolean("displayInBaseCurrency", displayInBaseCurrency).apply()
    }

    fun getDisplayInBaseCurrency(): Boolean {
        return sharedPreferences.getBoolean("displayInBaseCurrency", false)
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_enabled", false)
    }

    fun saveBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
    }
}
