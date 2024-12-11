package com.app.householdtracing.util

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import com.app.householdtracing.App
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.datastore.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

object AppUtil {

    private const val MAX_RETRY_ATTEMPTS = 5
    private const val RETRY_DELAY_MS = 3000L
    const val RADIUS = 1000
    const val GEOFENCE_RADIUS = 20.0f

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

    fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

}