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
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.datastore.PreferencesManager.LAST_API_LAT
import com.app.householdtracing.data.datastore.PreferencesManager.LAST_API_LNG
import com.app.householdtracing.data.model.GeofenceLocation
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.location.GeofenceManagerClient
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.GEOFENCE_RADIUS
import com.app.householdtracing.util.AppUtil.RADIUS
import com.app.householdtracing.util.AppUtil.distanceBetween
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.FusedLocationProvider
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
    private var currentGeofence: MutableList<GeofenceLocation> = mutableListOf()
    private var allApiRecords: MutableList<GeofenceLocation> = mutableListOf()

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
                callApiAndRegisterGeofence(currentLoc)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        userActivityTransitionManager.registerActivityTransitions()
        trackUserActivityAndLocation()
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
        scope.cancel()
    }

    private inline fun currentLocation(crossinline locationFound: (Location) -> Unit) {
        scope.launch {
            fusedLocationProvider.getCurrentLocation()?.let {
                showLogError(APP_TAG, it.longitude.toString())
                locationFound(it)
                PreferencesManager.putValue(LAST_API_LAT, it.latitude)
                PreferencesManager.putValue(LAST_API_LNG, it.longitude)
            }
        }
    }

    private suspend fun callApiAndRegisterGeofence(currentLoc: Location) {
        try {
            val response = getKoin().get<GeofencingRepository>().getCensusSubmissions(
                currentLoc.latitude, currentLoc.longitude, RADIUS
            )

            allApiRecords = response.map { loc ->
                GeofenceLocation(
                    loc._id,
                    loc.location_answer.coordinates[1],
                    loc.location_answer.coordinates[0]
                )
            }.toMutableList()
            notificationManager.setUpNotification(APP_TAG, "Total Shops : ${allApiRecords.size}")
            currentGeofence =
                allApiRecords.sortedBy { it.distanceTo(currentLoc) }.take(100).toMutableList()
            createGeofence(currentGeofence)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Recognition Activity
    private fun trackUserActivityAndLocation() {
        scope.launch {
            showLogError("$APP_TAG Service", "trackUserActivityAndLocation called")
            userActivityRepository.recognitionSharedFlow.collectLatest { event ->
                userActivityTransitionManager.observeUserActivity(event)
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
                notificationManager.setUpNotification(
                    channelId = AppNotificationManager.TRACKING_NOTIFICATION_CHANNEL_ID,
                    text = userActivityTransitionManager.getActivityMessage()
                )
                checkAndUpdateGeofence()
            }
        }
    }

    private fun checkAndUpdateGeofence() {
        currentLocation { newLocation ->
            scope.launch {
                val lastApiLat = PreferencesManager.getValue(LAST_API_LAT, 0.0).first()
                val lastApiLng = PreferencesManager.getValue(LAST_API_LNG, 0.0).first()
                val distance = distanceBetween(
                    lastApiLat,
                    lastApiLng,
                    newLocation.latitude,
                    newLocation.longitude
                )
                showLogError(
                    "$APP_TAG Geofence",
                    "New Location: ${newLocation.latitude}, ${newLocation.longitude} | " +
                            "Last API Location: $lastApiLat, $lastApiLng | Distance: $distance"
                )
                if (distance > RADIUS) {
                    PreferencesManager.putValue(LAST_API_LAT, newLocation.latitude)
                    PreferencesManager.putValue(LAST_API_LNG, newLocation.longitude)
                    showLogError("$APP_TAG Geofence", "Outside radius. Calling API...")
                    callApiAndRegisterGeofence(newLocation)
                    notificationManager.setUpNotification(
                        APP_TAG,
                        "Total Shops 2: ${allApiRecords.size}"
                    )
                } else {
                    showLogError("$APP_TAG Geofence", "Inside radius. Updating geofences...")
                    updateNearestGeofences(newLocation)
                }
            }
        }
    }

    private fun updateNearestGeofences(newLocation: Location) {
        currentGeofence =
            allApiRecords.sortedBy { it.distanceTo(newLocation) }.take(100).toMutableList()
        createGeofence(currentGeofence)
    }

    private fun createGeofence(geofenceLocations: List<GeofenceLocation>) {
        geofenceManagerClient.deregisterGeofence()
        geofenceManagerClient.addGeofence(
            requestIds = geofenceLocations.map { it.id },
            locations = geofenceLocations.map {
                Location("").apply {
                    latitude = it.latitude; longitude = it.longitude
                }
            },
            radiusInMeters = GEOFENCE_RADIUS
        )
        geofenceManagerClient.registerGeofence()
    }

}
