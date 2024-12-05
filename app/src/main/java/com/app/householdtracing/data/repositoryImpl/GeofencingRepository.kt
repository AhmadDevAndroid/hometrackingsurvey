package com.app.householdtracing.data.repositoryImpl

import com.app.householdtracing.data.model.responsedto.GeofencingStoresResponseBody
import com.app.householdtracing.network.services.HouseHoldApiService

class GeofencingRepository(private val apiService: HouseHoldApiService) {
    suspend fun getCensusSubmissions(
        lat: Double,
        lng: Double,
        radius: Int
    ): List<GeofencingStoresResponseBody> {
        return apiService.getCensusSubmissions(lat, lng, radius)
    }
}