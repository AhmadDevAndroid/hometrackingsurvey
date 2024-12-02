package com.app.householdtracing.network.services

import com.app.householdtracing.data.model.LoginResponseBody
import com.app.householdtracing.data.model.SunriseResponseBody
import com.app.householdtracing.data.model.requestdto.LoginRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HouseHoldApiService {

    @GET("json")
    suspend fun getSunriseSunset(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): SunriseResponseBody

    @POST("enumerators/authenticate")
    suspend fun authenticate(@Body credentials: LoginRequestBody): Response<LoginResponseBody>
}