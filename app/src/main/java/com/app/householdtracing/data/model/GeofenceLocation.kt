package com.app.householdtracing.data.model

import android.location.Location
import com.app.householdtracing.util.AppUtil.distanceBetween

data class GeofenceLocation(val id: String, val latitude: Double, val longitude: Double) {
    fun distanceTo(location: Location) =
        distanceBetween(location.latitude, location.longitude, latitude, longitude)
}
