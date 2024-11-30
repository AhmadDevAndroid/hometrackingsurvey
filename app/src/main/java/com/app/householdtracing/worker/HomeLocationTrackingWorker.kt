package com.app.householdtracing.worker

import android.content.Context
import android.location.Location
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.model.HomeTrackingLocations
import com.app.householdtracing.data.repositoryImpl.SunriseRepositoryImpl
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppNotificationManager.Companion.LOCATION_WORKER_CHANNEL_ID
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.FusedLocationProvider
import org.koin.java.KoinJavaComponent.getKoin

class HomeLocationTrackingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = AppNotificationManager(context)
    private val repo: SunriseRepositoryImpl by lazy { getKoin().get<SunriseRepositoryImpl>() }
    private val fusedLocationProvider by lazy { FusedLocationProvider(context) }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        notificationManager.createNotificationChannel(LOCATION_WORKER_CHANNEL_ID)
        return ForegroundInfo(
            LOCATION_WORKER_CHANNEL_ID.hashCode(),
            notificationManager.createForegroundNotification(LOCATION_WORKER_CHANNEL_ID)
        )
    }

    companion object {
        private const val TAG = "HomeLocationTrackingWorker"
        private const val LOCATION_LIMIT = 9
        private const val TIME_KEY = "time"
        const val DISTANCE_THRESHOLD = 50

        fun configureWorker(context: Context, time: Long) {
            val inputData = Data.Builder()
                .putLong(TIME_KEY, time)
                .build()

            val request = OneTimeWorkRequestBuilder<HomeLocationTrackingWorker>()
                .setConstraints(Constraints.NONE)
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, request)
        }
    }

    override suspend fun doWork(): Result {
        if (!canInsertLocation()) {
            showLogError(TAG, "Location limit reached or conditions not met!")
            return Result.success()
        }

        val currentLocation = fusedLocationProvider.getCurrentLocation()
        if (currentLocation == null) {
            notificationManager.setUpNotification(
                LOCATION_WORKER_CHANNEL_ID,
                "Failed to fetch location."
            )
            return Result.retry()
        }

        processLocation(currentLocation)
        return Result.success()
    }

    private suspend fun canInsertLocation(): Boolean {
        val savedLocations = repo.getHomeTrackingLocations()
        return savedLocations.size < LOCATION_LIMIT
    }

    private suspend fun processLocation(location: Location) {
        val savedLocations = repo.getHomeTrackingLocations()

        compareLocation(
            location,
            savedLocations,
            onLocationDiscovered = {
                repo.insertHomeTrackingLocation(
                    createHomeTrackingLocation(location)
                )
                saveLocationInfo(location)
                repo.deleteAll()
                notificationManager.setUpNotification(
                    LOCATION_WORKER_CHANNEL_ID,
                    "Location Found: ${location.latitude}, ${location.longitude}"
                )
            },
            notBetweenLocation = {
                repo.deleteAndInsertNew(createHomeTrackingLocation(location))
                notificationManager.setUpNotification(
                    LOCATION_WORKER_CHANNEL_ID,
                    "Location Not Within Range."
                )
            }
        )
    }

    private fun createHomeTrackingLocation(location: Location) = HomeTrackingLocations(
        milliseconds = System.currentTimeMillis(),
        latitude = location.latitude,
        longitude = location.longitude
    )

    private suspend inline fun compareLocation(
        referenceLocation: Location,
        savedLocations: List<HomeTrackingLocations>,
        crossinline onLocationDiscovered: suspend () -> Unit,
        crossinline notBetweenLocation: suspend () -> Unit
    ) {
        if (isLocationWithinRange(referenceLocation, savedLocations)) {
            onLocationDiscovered()
        } else {
            notBetweenLocation()
        }
    }

    private fun isLocationWithinRange(
        referenceLocation: Location,
        savedLocations: List<HomeTrackingLocations>
    ): Boolean {
        savedLocations.forEach { savedLocation ->
            val savedLocationAsObject = Location("SavedLocation").apply {
                latitude = savedLocation.latitude
                longitude = savedLocation.longitude
            }
            val distance = referenceLocation.distanceTo(savedLocationAsObject)
            showLogError("Distance Check", "Distance: $distance meters")
            if (distance > DISTANCE_THRESHOLD) return false
        }
        return true
    }

    private suspend fun saveLocationInfo(it: Location) {
        PreferencesManager.putValue(PreferencesManager.isLocationFound, true)
        PreferencesManager.putValue(PreferencesManager.latitude, it.longitude)
        PreferencesManager.putValue(PreferencesManager.longitude, it.longitude)
    }
}