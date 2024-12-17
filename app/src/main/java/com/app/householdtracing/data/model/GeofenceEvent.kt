package com.app.householdtracing.data.model

data class GeofenceEvent(
    val requestId: String,
    val type: GeofenceTransitionType,
    val timestamp: String
)

enum class GeofenceTransitionType {
    ENTER, EXIT
}
