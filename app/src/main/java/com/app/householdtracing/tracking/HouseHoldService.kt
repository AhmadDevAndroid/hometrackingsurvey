package com.app.householdtracing.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.model.GeofenceTransitionType
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.location.GeofenceManagerClient
import com.app.householdtracing.sensors.SensorDetectionManager
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.showLogError
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin


class HouseHoldService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val binder = LocalBinder()
    private val notificationManager by lazy { AppNotificationManager(this) }
    private val geofenceManagerClient by lazy { GeofenceManagerClient(this) }
    private val userActivityTransitionManager by lazy { UserActivityTransitionManager(this) }
    private val userActivityRepository: UserActivityTrackingRepository by lazy { getKoin().get() }
    private val sensorDetectionManager by lazy { SensorDetectionManager(this) }

    inner class LocalBinder : Binder() {
        val service: HouseHoldService get() = this@HouseHoldService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()

//        scope.launch {
//            PreferencesManager.getValue(PreferencesManager.SUNRISE_TIME, 0).collectLatest {
//                SunriseTrackingWorker.configureWorker(this@HouseHoldService)
//            }
//        }TODO() Chnages

        geofenceManagerClient.currentLocation { currentLoc ->
            scope.launch {
                showLogError(
                    "$APP_TAG Geofence",
                    "current: ${currentLoc.latitude},${currentLoc.longitude}"
                )
                geofenceManagerClient.callApiAndRegisterGeofence(currentLoc)
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
                geofenceManagerClient.checkAndUpdateGeofence()
                observeGeofenceTransitions()
            }
        }
    }


    //Geofence Observe Events
    private fun observeGeofenceTransitions() {
        scope.launch {
            userActivityRepository.geofenceEvents.collectLatest { event ->
                val eventMessage = when (event.type) {
                    GeofenceTransitionType.ENTER -> {
                        sensorDetectionManager.startSensorDetection()
                        "Entered geofence [${event.requestId}] at ${event.timestamp}"
                    }

                    GeofenceTransitionType.EXIT -> {
                        sensorDetectionManager.stopSensorDetection()
                        "Exited geofence [${event.requestId}] at ${event.timestamp}"
                    }
                }

                showLogError(APP_TAG, eventMessage)

                notificationManager.setUpNotification(
                    "GeoFence Event",
                    eventMessage
                )
            }
        }
    }

}
