package com.app.householdtracing.util

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import com.app.householdtracing.App
import kotlinx.coroutines.delay
import timber.log.Timber

object AppUtil {

    private const val MAX_RETRY_ATTEMPTS = 5
    private const val RETRY_DELAY_MS = 3000L

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
                Timber.tag(TAG).i("Api succeeded after ${attempt + 1} attempts")
                return result
            }
            Timber.tag(TAG).e("Retrying Api, attempt: ${attempt + 1}")
            delay(delayMs)
        }
        Timber.tag(TAG).e("Max retries reached, returning failure")
        return androidx.work.ListenableWorker.Result.failure()
    }

}