package com.app.householdtracing.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.app.householdtracing.App.Companion.APP_TAG
import com.app.householdtracing.data.datastore.PreferencesManager
import com.app.householdtracing.data.datastore.PreferencesManager.LAST_API_LAT
import com.app.householdtracing.data.datastore.PreferencesManager.LAST_API_LNG
import com.app.householdtracing.data.model.GeofenceLocation
import com.app.householdtracing.data.repositoryImpl.GeofencingRepository
import com.app.householdtracing.receiver.GeofenceBroadcastReceiver
import com.app.householdtracing.util.AppNotificationManager
import com.app.householdtracing.util.AppUtil.GEOFENCE_RADIUS
import com.app.householdtracing.util.AppUtil.RADIUS
import com.app.householdtracing.util.AppUtil.distanceBetween
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.FusedLocationProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class GeofenceManagerClient(private val context: Context) {
    private val client = LocationServices.getGeofencingClient(context)
    private val geofenceList = mutableListOf<Geofence>()
    private var currentGeofence: MutableList<GeofenceLocation> = mutableListOf()
    private var allApiRecords: MutableList<GeofenceLocation> = mutableListOf()
    private val notificationManager by lazy { AppNotificationManager(context) }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fusedLocationProvider by lazy { FusedLocationProvider(context) }

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

    private fun addGeofence(
        requestIds: List<String>,
        locations: List<Location>,
        radiusInMeters: Float
    ) {
        if (requestIds.size != locations.size) {
            throw IllegalArgumentException("The number of requestIds must match the number of locations")
        }
        requestIds.zip(locations).forEachIndexed { _, (requestId, location) ->
            val geofence = createGeofence(requestId, location, radiusInMeters)
            geofenceList.add(geofence)
        }
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

    private fun deregisterGeofence() {
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

    suspend fun callApiAndRegisterGeofence(currentLoc: Location) {
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

    fun checkAndUpdateGeofence() {
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
                PreferencesManager.putValue(LAST_API_LAT, newLocation.latitude)
                PreferencesManager.putValue(LAST_API_LNG, newLocation.longitude)
                showLogError(
                    "$APP_TAG Geofence",
                    "New Location: ${newLocation.latitude}, ${newLocation.longitude} | " +
                            "Last API Location: $lastApiLat, $lastApiLng | Distance: $distance"
                )
                when {
                    distance > RADIUS -> {
                        showLogError("$APP_TAG Geofence", "Outside radius. Calling API...")
                        callApiAndRegisterGeofence(newLocation)
                        notificationManager.setUpNotification(
                            APP_TAG, "Total Shops updated: ${allApiRecords.size}"
                        )
                    }

                    distance > GEOFENCE_RADIUS -> {
                        showLogError("$APP_TAG Geofence", "Inside radius. Updating geofences...")
                        updateNearestGeofences(newLocation)
                        notificationManager.setUpNotification(APP_TAG, "GeoFence Updated")
                    }

                    else -> {
                        showLogError(
                            "$APP_TAG Geofence",
                            "User is at the same location. No update needed."
                        )
                        notificationManager.setUpNotification(
                            APP_TAG,
                            "User is at the same location. No update needed."
                        )
                    }
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
        deregisterGeofence()
        addGeofence(
            requestIds = geofenceLocations.map { it.id },
            locations = geofenceLocations.map {
                Location("").apply {
                    latitude = it.latitude; longitude = it.longitude
                }
            },
            radiusInMeters = GEOFENCE_RADIUS
        )
        registerGeofence()
    }

    fun currentLocation(locationFound: (Location) -> Unit) {
        scope.launch {
            fusedLocationProvider.getCurrentLocation()?.let {
                showLogError(APP_TAG, it.longitude.toString())
                locationFound(it)
                PreferencesManager.putValue(LAST_API_LAT, it.latitude)
                PreferencesManager.putValue(LAST_API_LNG, it.longitude)
            }
        }
    }
}
