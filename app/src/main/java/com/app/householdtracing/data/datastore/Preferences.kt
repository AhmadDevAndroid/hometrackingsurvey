package com.app.householdtracing.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.householdtracing.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesManager {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = App.getInstance().packageName
    )

    private val context = App.getInstance()
    val SUNRISE_TIME = longPreferencesKey("sunrise_time")
    val LATITUDE = doublePreferencesKey("latitude")
    val LONGITUDE = doublePreferencesKey("longitude")
    val isLocationFound = booleanPreferencesKey("location_found")
    val LAST_API_LAT = doublePreferencesKey("LastApiLat")
    val LAST_API_LNG = doublePreferencesKey("LastApiLng")

    suspend fun <T> putValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

}