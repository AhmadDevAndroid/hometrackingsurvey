package com.app.householdtracing.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_tracking_locations")
data class HomeTrackingLocations(
    @PrimaryKey var milliseconds: Long,
    val latitude: Double,
    val longitude: Double
)