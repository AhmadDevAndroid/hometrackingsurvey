package com.app.householdtracing.data.repositoryImpl

import com.app.householdtracing.data.model.LoginResponseBody
import com.app.householdtracing.data.model.requestdto.LoginRequestBody
import com.app.householdtracing.network.services.HouseHoldApiService

class AuthRepositoryImpl(private val api: HouseHoldApiService) {
    suspend fun authenticate(identifier: String, password: String): Result<LoginResponseBody> {
        return try {
            val response = api.authenticate(LoginRequestBody(identifier, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Throwable(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
