package com.app.householdtracing.network.services

import com.app.householdtracing.data.model.requestdto.LoginRequestBody
import com.app.householdtracing.data.model.responsedto.GeofencingStoresResponseBody
import com.app.householdtracing.data.model.responsedto.LoginResponseBody
import com.app.householdtracing.data.model.responsedto.SunriseResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface HouseHoldApiService {

    // @GET("json")
    suspend fun getSunriseSunset(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): SunriseResponseBody

    @POST
    suspend fun authenticate(@Url string: String, @Body credentials: LoginRequestBody): Response<LoginResponseBody>

    @GET("census/getCensusSubmissions")
    suspend fun getCensusSubmissions(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int
    ): List<GeofencingStoresResponseBody>
}