package com.app.householdtracing.data.repositoryImpl

import app.household.data.db.dao.HomeTrackingDao
import com.app.householdtracing.data.model.HomeTrackingLocations
import com.app.householdtracing.data.model.SunriseResponseBody
import com.app.householdtracing.network.converter.Resource
import com.app.householdtracing.network.services.HouseHoldApiService

class SunriseRepositoryImpl(
    private val sunriseApi: HouseHoldApiService,
    private val homeTrackingDao: HomeTrackingDao
) : BaseRepository() {

    suspend fun callSunriseApi(
        latitude: Double,
        longitude: Double
    ): Resource<SunriseResponseBody> {
        return doApiCall {
            sunriseApi.getSunriseSunset(latitude, longitude)
        }
    }

    suspend fun getHomeTrackingLocations() = homeTrackingDao.getAll()

    suspend fun insertHomeTrackingLocation(homeTrackingLocations: HomeTrackingLocations) {
        homeTrackingDao.insert(homeTrackingLocations)
    }

    suspend fun deleteAndInsertNew(homeTrackingLocations: HomeTrackingLocations) {
        homeTrackingDao.deleteAndInsertNew(homeTrackingLocations)
    }

    suspend fun deleteAll() {
        homeTrackingDao.deleteAll()
    }
}