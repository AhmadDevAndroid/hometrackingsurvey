package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.model.GeofenceEvent
import com.app.householdtracing.data.model.GeofenceTransitionType
import com.app.householdtracing.data.repositoryImpl.UserActivityTrackingRepository
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.DateUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val userActivityRepository: UserActivityTrackingRepository by lazy { getKoin().get() }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            showLogError("$APP_TAG GeoFenceReceiver", errorMessage)
            return
        }
        val transitionType = geofencingEvent?.geofenceTransition ?: return
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val currentTime = DateUtil.getHourAndMinute()

        // Get the transition type
        triggeringGeofences.forEach { geofence ->
            val event = when (transitionType) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> GeofenceEvent(
                    requestId = geofence.requestId,
                    type = GeofenceTransitionType.ENTER,
                    timestamp = currentTime
                )

                Geofence.GEOFENCE_TRANSITION_EXIT -> GeofenceEvent(
                    requestId = geofence.requestId,
                    type = GeofenceTransitionType.EXIT,
                    timestamp = currentTime
                )

                else -> null
            }

            scope.launch {
                event?.let { userActivityRepository.postGeofenceEvent(it) }
            }
        }
    }
}
