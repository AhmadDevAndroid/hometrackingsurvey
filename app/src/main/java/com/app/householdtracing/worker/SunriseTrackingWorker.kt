package com.app.householdtracing.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.model.responsedto.SunriseResponseBody.SunriseResults
import com.app.householdtracing.data.repositoryImpl.SunriseRepositoryImpl
import com.app.householdtracing.network.converter.Status
import com.app.householdtracing.util.AlarmManager
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppNotificationManager.Companion.WORKER_CHANNEL_ID
import com.app.householdtracing.util.AppUtil.retryUntilSuccess
import com.app.householdtracing.util.DateUtil
import com.app.householdtracing.util.DateUtil.nextAlarmTimes
import com.app.householdtracing.util.DateUtil.parseTime
import com.app.householdtracing.util.FusedLocationProvider
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import java.util.Calendar

class SunriseTrackingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repo: SunriseRepositoryImpl by lazy { getKoin().get() }
    private val notificationManager: AppNotificationManager by lazy { AppNotificationManager(context) }
    private val fusedLocationProvider: FusedLocationProvider by lazy { FusedLocationProvider(context) }
    private val alarmManager: AlarmManager by lazy { AlarmManager(context) }

    companion object {
        private const val TAG = "SunriseTrackingWorker"

        fun configureWorker(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<SunriseTrackingWorker>()
                .setConstraints(Constraints.NONE)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                oneTimeRequest
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        notificationManager.createNotificationChannel(WORKER_CHANNEL_ID)
        return ForegroundInfo(
            WORKER_CHANNEL_ID.hashCode(),
            notificationManager.createForegroundNotification(WORKER_CHANNEL_ID)
        )
    }

    override suspend fun doWork(): Result {
        return retryUntilSuccess {
            fetchAndProcessSunriseData()
        }
    }

    private suspend fun fetchAndProcessSunriseData(): Result {
        val location = fusedLocationProvider.getCurrentLocation()
        if (location == null) {
            Timber.tag(TAG).e("Failed to fetch location")
            notificationManager.setUpNotification("Location Error", "Failed to fetch location")
            return Result.retry()
        }

        val apiResponse = repo.callSunriseApi(
            latitude = location.latitude,
            longitude = location.longitude
        )

        return when (apiResponse.status) {
            Status.SUCCESS -> {
                val results = apiResponse.data?.results ?: return Result.failure()
                processSunriseResults(results)
                Result.success()
            }

            else -> {
                Timber.tag(TAG).e("Failed to fetch sunrise data")
                notificationManager.setUpNotification("API Error", "Failed to fetch sunrise data")
                Result.retry()
            }
        }
    }

    private suspend fun processSunriseResults(results: SunriseResults) {
        val sunriseTime = parseTime(results.sunrise)
        //val currentTimeInMillis = System.currentTimeMillis()
        //val tenMinutesLater = currentTimeInMillis + 10 * 60 * 1000
        val calendar = Calendar.getInstance()
        if (sunriseTime <= 0) {
            Timber.tag(TAG).e("Invalid sunrise time")
            return
        }

        PreferencesManager.putValue(PreferencesManager.SUNRISE_TIME, sunriseTime)
        notificationManager.setUpNotification(
            "Sunrise Updated",
            "Sunrise: ${results.sunrise}, Sunset: ${results.sunset}"
        )
        scheduleNextAlarm(results, calendar)
        //scheduleNextAlarm(tenMinutesLater, calendar)
    }

    private fun scheduleNextAlarm(results: SunriseResults, calendar: Calendar) {
        val sunriseCalendar = DateUtil.getMillisecondsFromDate(calendar, results.sunrise)
        val currentTimeInMillis = System.currentTimeMillis()
        val availableTime = sunriseCalendar.timeInMillis - currentTimeInMillis
        val alarmTimes = nextAlarmTimes(sunriseCalendar, availableTime)

        alarmTimes.forEach { alarmTime ->
            alarmManager.scheduleNextAlarmTime(alarmTime, sunriseCalendar.timeInMillis)
        }

//    private fun scheduleNextAlarm(alarmTime: Long, calendar: Calendar) {
//        // Use the current time as a base for testing
//        val currentTimeInMillis = System.currentTimeMillis()
//        val tenMinutesLater = currentTimeInMillis + 10 * 60 * 1000
//        val dateFormat = SimpleDateFormat(DateUtil.TIME_PATTERN, Locale.getDefault())
//        val formattedTime = dateFormat.format(Date(tenMinutesLater))
//        val sunriseCalendar = DateUtil.getMillisecondsFromDate(calendar, formattedTime)
//        val availableTime = sunriseCalendar.timeInMillis - currentTimeInMillis
//        val alarmTimes = nextAlarmTimes(sunriseCalendar, availableTime)
//        Timber.tag("Alarm Test").d("Current time: $currentTimeInMillis, Alarm time: $alarmTime")
//
//        // Schedule the alarm
//
//        alarmTimes.forEach { alarm ->
//            alarmManager.scheduleNextAlarmTime(alarm, sunriseCalendar.timeInMillis)
//        }
//    }
    }
}
