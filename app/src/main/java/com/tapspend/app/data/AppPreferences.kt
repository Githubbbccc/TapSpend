package com.tapspend.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tapspend_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_SALARY = doublePreferencesKey("monthly_salary")
        val KEY_CURRENCY = stringPreferencesKey("app_currency")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val monthlySalaryFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_SALARY] ?: 60000.0 // Default 60,000 PKR
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "PKR"
    }

    val notificationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION_ENABLED] ?: true
    }

    suspend fun updateSalary(salary: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SALARY] = salary
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }
}
