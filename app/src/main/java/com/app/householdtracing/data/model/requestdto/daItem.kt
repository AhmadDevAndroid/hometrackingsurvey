package com.app.householdtracing.data.model.requestdto

data class daItem(
    val _id: String,
    val location_answer: LocationAnswer,
    val outlet_types: String,
    val poi: String,
    val store_name: String
)