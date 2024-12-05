package com.app.householdtracing.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppNotificationManager.Companion.GEOFENCE_CHANNEL_ID
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager by lazy { AppNotificationManager(context) }
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Timber.tag(TAG).e(errorMessage)
            return
        }
        // Get the transition type.
        when (geofencingEvent?.geofenceTransition) {

            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                notificationManager.setUpNotification(GEOFENCE_CHANNEL_ID, "Enterance")
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                notificationManager.setUpNotification(GEOFENCE_CHANNEL_ID, "Dwell")
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                notificationManager.setUpNotification(GEOFENCE_CHANNEL_ID, "Exit")
            }

            else -> {
                Timber.tag(TAG).e("Error in setting up the geofence")
            }
        }
    }

}
