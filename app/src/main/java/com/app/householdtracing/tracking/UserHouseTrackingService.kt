package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.location.GeofenceManagerClient
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.GEOFENCE_RADIUS
import com.app.householdtracing.util.AppUtil.RADIUS
import com.app.householdtracing.util.AppUtil.isWithinGeofence
import com.app.householdtracing.util.AppUtil.saveGeofence
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.FusedLocationProvider
import com.google.android.gms.location.DetectedActivity
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
    private val geofenceManagerClient by lazy { GeofenceManagerClient(this) }
    private val userActivityTransitionManager by lazy { UserActivityTransitionManager(this) }
    private val fusedLocationProvider by lazy { FusedLocationProvider(this) }
    private val userActivityRepository: UserActivityTrackingRepository by lazy { getKoin().get() }

    inner class LocalBinder : Binder() {
        val service: UserHouseTrackingService get() = this@UserHouseTrackingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()

//        scope.launch {
//            PreferencesManager.getValue(PreferencesManager.SUNRISE_TIME, 0).collectLatest {
//                SunriseTrackingWorker.configureWorker(this@UserHouseTrackingService)
//            }
//        }TODO() Chnages

        currentLocation { currentLoc ->
            scope.launch {
                showLogError(
                    "$APP_TAG Geofence",
                    "current: ${currentLoc.latitude},${currentLoc.longitude}"
                )
                latLngApiCall(currentLoc.latitude, currentLoc.longitude, RADIUS)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        trackUserActivityAndLocation()
        userActivityTransitionManager.registerActivityTransitions()
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
                showLogError(APP_TAG, it.longitude.toString())
                locationFound(it)
                saveGeofence(it.latitude, it.longitude, GEOFENCE_RADIUS)
            }
        }
    }

    private suspend fun latLngApiCall(lat: Double, lng: Double, radius: Int) {
        val geofencingRepository: GeofencingRepository by lazy { getKoin().get() }
        try {
            val response = geofencingRepository.getCensusSubmissions(lat, lng, radius)
            val requestIds = response.map { loc -> loc._id }
            val geofenceLocations = response.map { loc ->
                Location("").apply {
                    latitude = loc.location_answer.coordinates[1]
                    longitude = loc.location_answer.coordinates[0]
                    showLogError(APP_TAG, "$latitude, $longitude")
                }
            }

            geofenceManagerClient.addGeofences(
                requestIds = requestIds,
                locations = geofenceLocations,
                radiusInMeters = GEOFENCE_RADIUS
            )
            showLogError(APP_TAG, "$geofenceLocations")
            geofenceManagerClient.registerGeofence()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Recognition Activity
    private fun trackUserActivityAndLocation() {
        scope.launch {
            userActivityRepository.recognitionSharedFlow.collectLatest { event ->
                userActivityTransitionManager.observeUserActivity(event)
                notificationManager.setUpNotification(
                    channelId = AppNotificationManager.TRACKING_NOTIFICATION_CHANNEL_ID,
                    text = userActivityTransitionManager.getActivityMessage()
                )
                when {
                    userActivityTransitionManager.isInVehicle() -> userActivityTransitionManager.switchToActivity(
                        DetectedActivity.IN_VEHICLE
                    )

                    userActivityTransitionManager.isOnFoot() -> userActivityTransitionManager.switchToActivity(
                        DetectedActivity.ON_FOOT
                    )

                    userActivityTransitionManager.isStill() -> userActivityTransitionManager.switchToActivity(
                        DetectedActivity.STILL,
                        true
                    )
                }
                checkAndCallApiOnLocationUpdate()
            }
        }
    }

    private fun checkAndCallApiOnLocationUpdate() {
        currentLocation { newLocation ->
            scope.launch {
                isWithinGeofence(newLocation).collect { isInside ->
                    if (!isInside) {
                        latLngApiCall(newLocation.latitude, newLocation.longitude, RADIUS)
                        notificationManager.setUpNotification(
                            "$APP_TAG Geofence",
                            "Location is outside the geofence."
                        )
                    } else {
                        showLogError("$APP_TAG Geofence", "Location is inside the geofence.")
                    }
                }
            }
        }
    }

}
