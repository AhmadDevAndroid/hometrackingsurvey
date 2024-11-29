package com.app.householdtracing.network.services

import com.app.householdtracing.data.model.SunriseResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface SunriseApiService {

    @GET("json")
    suspend fun getSunriseSunset(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): SunriseResponseBody
}