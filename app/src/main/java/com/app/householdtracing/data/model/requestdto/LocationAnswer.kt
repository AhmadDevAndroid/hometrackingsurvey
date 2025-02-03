package com.app.householdtracing.data.model.requestdto

data class LocationAnswer(
    val _id: String,
    val accuracy: Double,
    val coordinates: List<Double>,
    val isLastLocationPicked: Boolean,
    val title: String,
    val type: String
)