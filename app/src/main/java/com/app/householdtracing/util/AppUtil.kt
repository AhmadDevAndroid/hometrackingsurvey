package com.app.householdtracing.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import com.app.householdtracing.App
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.datastore.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber

object AppUtil {

    private const val MAX_RETRY_ATTEMPTS = 5
    private const val RETRY_DELAY_MS = 3000L
    const val RADIUS = 90
    const val GEOFENCE_RADIUS = 50.0f

    fun showLogError(tag: String, msg: String) {
        Timber.tag(tag).e(msg)
    }

    fun showToastMsg(msg: String) {
        Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT).show()
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    @SuppressLint("RestrictedApi")
    suspend fun retryUntilSuccess(
        maxAttempts: Int = MAX_RETRY_ATTEMPTS,
        delayMs: Long = RETRY_DELAY_MS,
        action: suspend () -> androidx.work.ListenableWorker.Result
    ): androidx.work.ListenableWorker.Result {
        repeat(maxAttempts) { attempt ->
            val result = action()
            if (result is androidx.work.ListenableWorker.Result.Success) {
                Timber.tag(APP_TAG).i("Api succeeded after ${attempt + 1} attempts")
                return result
            }
            Timber.tag(APP_TAG).e("Retrying Api, attempt: ${attempt + 1}")
            delay(delayMs)
        }
        Timber.tag(APP_TAG).e("Max retries reached, returning failure")
        return androidx.work.ListenableWorker.Result.failure()
    }

    suspend fun saveGeofence(latitude: Double, longitude: Double, radius: Float) {
        PreferencesManager.putValue(PreferencesManager.GEOFENCE_LATITUDE, latitude)
        PreferencesManager.putValue(PreferencesManager.GEOFENCE_LONGITUDE, longitude)
        PreferencesManager.putValue(PreferencesManager.GEOFENCE_RADIUS, radius)
    }

    fun getGeofence(): Flow<Triple<Double, Double, Float>> {
        val latitude = PreferencesManager.getValue(PreferencesManager.GEOFENCE_LATITUDE, 0.0)
        val longitude = PreferencesManager.getValue(PreferencesManager.GEOFENCE_LONGITUDE, 0.0)
        val geofenceRadius = PreferencesManager.getValue(PreferencesManager.GEOFENCE_RADIUS, 0.0f)

        return combine(latitude, longitude, geofenceRadius) { lat, lng, radius ->
            Triple(lat, lng, radius)
        }
    }

    fun isWithinGeofence(context: Context, newLocation: Location): Flow<Boolean> {
        return getGeofence().map { (savedLat, savedLng, radius) ->
            if (savedLat == 0.0 && savedLng == 0.0) {
                false
            } else {
                val savedLocation = Location("").apply {
                    latitude = savedLat
                    longitude = savedLng
                }
                savedLocation.distanceTo(newLocation) <= radius
            }
        }
    }

}