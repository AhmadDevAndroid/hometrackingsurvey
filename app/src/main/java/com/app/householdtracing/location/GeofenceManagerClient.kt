package com.app.householdtracing.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.receiver.GeofenceBroadcastReceiver
import com.app.householdtracing.util.AppUtil.showLogError
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManagerClient(private val context: Context) {
    private val client = LocationServices.getGeofencingClient(context)
    private val geofenceList = mutableListOf<Geofence>()

    companion object {
        const val CUSTOM_REQUEST_CODE_GEOFENCE = 1001
    }

    private val receiverIntent by lazy {
        Intent(context, GeofenceBroadcastReceiver::class.java)
    }

    private val geofencingPendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            receiverIntent,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    fun addGeofences(
        requestIds: List<String>,
        locations: List<Location>,
        radiusInMeters: Float
    ) {
        if (requestIds.size != locations.size) {
            throw IllegalArgumentException("The number of requestIds must match the number of locations")
        }

        val geofence = createGeofence("maja_rica", Location("").apply {
            latitude = 31.518693
            longitude = 74.323877
        }, radiusInMeters)
        geofenceList.add(geofence)

        /*  requestIds.zip(locations).forEachIndexed { index, (requestId, location) ->
              val geofence = createGeofence("item_$index", location, radiusInMeters)
              geofenceList.add(geofence)
          }*/
    }

    @SuppressLint("MissingPermission")
    fun registerGeofence() {
        if (geofenceList.isEmpty()) {
            showLogError(
                "$APP_TAG GeoFenceManagerClient",
                "registerGeofence: No geofences to register."
            )
            return
        }

        client.addGeofences(createGeofencingRequest(), geofencingPendingIntent).run {
            addOnSuccessListener {
                showLogError("$APP_TAG GeoFenceManagerClient", "registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                showLogError(
                    "$APP_TAG GeoFenceManagerClient", "registerGeofence: Failure\n$exception"
                )
            }
        }
    }

    fun deregisterGeofence() {
        client.removeGeofences(geofencingPendingIntent)
        geofenceList.clear()
        showLogError("$APP_TAG GeoFenceManagerClient", "deregisterGeofence: All geofence removed.")
    }

    private fun createGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun createGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }
}
