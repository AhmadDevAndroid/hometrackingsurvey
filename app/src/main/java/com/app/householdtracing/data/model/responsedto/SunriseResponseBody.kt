package com.app.householdtracing.data.model.responsedto

data class SunriseResponseBody(
    val results: SunriseResults,
    val status: String
) {
    data class SunriseResults(
        val date: String,
        val dawn: String,
        val day_length: String,
        val dusk: String,
        val first_light: String,
        val golden_hour: String,
        val last_light: String,
        val solar_noon: String,
        val sunrise: String,
        val sunset: String,
        val timezone: String,
        val utc_offset: Int
    )
}
