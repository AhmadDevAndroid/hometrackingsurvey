package com.app.householdtracing.data.model.responsedto

data class GeofencingStoresResponseBody(
    val _id: String,
    val location_answer: LocationAnswer
) {
    data class LocationAnswer(
        val _id: String,
        val accuracy: Double,
        val coordinates: List<Double>,
        val isLastLocationPicked: Boolean,
        val title: String,
        val type: String
    )
}
