package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.AppUtil.showToastMsg
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager by lazy { AppNotificationManager(context) }
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            showLogError("$APP_TAG GeoFenceReceiver", errorMessage)
            return
        }
        val transationType = geofencingEvent?.geofenceTransition
        val currentTime = System.currentTimeMillis()
        // Get the transition type
        when (transationType) {

            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                geofencingEvent.triggeringGeofences?.forEach { item ->
                    item.requestId
                    showLogError("$APP_TAG GeoFenceReceiver", "Enter ID: ${item.requestId} at $currentTime")
                }
                notificationManager.setUpNotification("GeoFence Enter", "Enter at $currentTime")
            }


            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                geofencingEvent.triggeringGeofences?.forEach { item ->
                    item.requestId
                    showLogError("$APP_TAG GeoFenceReceiver", "Exit requestId: ${item.requestId} at $currentTime")
                }
                notificationManager.setUpNotification("GeoFence Exit", "Exit at $currentTime")
            }

            else -> {
                showLogError("$APP_TAG GeoFenceReceiver", "Error in setting up the geofence")
            }
        }
    }

}
