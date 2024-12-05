package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.location.GeofenceManagerClient
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.GEOFENCE_RADIUS
import com.app.householdtracing.util.AppUtil.RADIUS
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.FusedLocationProvider
import com.app.householdtracing.worker.SunriseTrackingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin


class UserHouseTrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val binder = LocalBinder()
    private val notificationManager by lazy { AppNotificationManager(this) }
    private val geofenceManagerClient by lazy {
        GeofenceManagerClient(this)
    }
    private val userActivityTransitionManager by lazy {
        UserActivityTransitionManager(this)
    }

    private val fusedLocationProvider by lazy { FusedLocationProvider(this) }

    inner class LocalBinder : Binder() {
        val service: UserHouseTrackingService get() = this@UserHouseTrackingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            PreferencesManager.getValue(PreferencesManager.SUNRISE_TIME, 0).collectLatest {
                SunriseTrackingWorker.configureWorker(this@UserHouseTrackingService)
            }
        }

        currentLocation { location ->
            scope.launch {
                latLngApiCall(location.latitude, location.longitude, RADIUS)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        userActivityTransitionManager.registerActivityTransitions()
        geofenceManagerClient.registerGeofence()
        return START_STICKY
    }

    @SuppressLint("NewApi")
    private fun startForegroundService() {
        val notification = notificationManager.setUpNotification(
            channelId = AppNotificationManager.TRACKING_NOTIFICATION_CHANNEL_ID
        )
        val foregroundServiceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                FOREGROUND_SERVICE_TYPE_LOCATION
            } else 0

        startForeground(1, notification.build(), foregroundServiceType)
    }

    override fun onDestroy() {
        super.onDestroy()
        userActivityTransitionManager.deregisterActivityTransitions()
        geofenceManagerClient.deregisterGeofence()
        scope.cancel()
    }

    private inline fun currentLocation(crossinline locationFound: (Location) -> Unit) {
        scope.launch {
            fusedLocationProvider.getCurrentLocation()?.let {
                showLogError("location", it.longitude.toString())
                locationFound(it)
            }
        }
    }

    private suspend fun latLngApiCall(lat: Double, lng: Double, radius: Int) {
        val geofencingRepository: GeofencingRepository by lazy { getKoin().get() }

        try {
            val response = geofencingRepository.getCensusSubmissions(lat, lng, radius)
            response.forEach { item ->
                val key = item._id
                val locationAnswer = item.location_answer
                val coordinates = locationAnswer.coordinates

                val location = Location("").apply {
                    latitude = coordinates[0]
                    longitude = coordinates[1]
                }
                geofenceManagerClient.addGeofence(
                    key = key,
                    location = location,
                    radiusInMeters = GEOFENCE_RADIUS
                )
                println("type: ${item.location_answer}, location: ${item.location_answer.coordinates}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
