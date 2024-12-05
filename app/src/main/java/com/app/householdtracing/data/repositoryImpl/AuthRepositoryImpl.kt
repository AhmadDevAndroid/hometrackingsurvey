package com.app.householdtracing.data.repositoryImpl

import androidx.datastore.core.DataStore
import com.app.householdtracing.LoginResponse
import com.app.householdtracing.data.model.responsedto.LoginResponseBody
import com.app.householdtracing.data.model.requestdto.LoginRequestBody
import com.app.householdtracing.network.services.HouseHoldApiService
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(private val dataStore: DataStore<LoginResponse>,private val api: HouseHoldApiService) {
    suspend fun authenticate(identifier: String, password: String): Result<LoginResponseBody> {
        return try {
            val response = api.authenticate(LoginRequestBody(identifier, password))
            if (response.isSuccessful && response.body() != null) {
                saveUser(response.body()!!)
                Result.success(response.body()!!)
            } else {
                Result.failure(Throwable(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUser(loginResponseBody: LoginResponseBody){
        dataStore.updateData {
            it.toBuilder().setToken(loginResponseBody.token)
                .build()
        }
    }

     fun getUser() = dataStore.data.map {
        LoginResponseBody(it.token ?: "")
    }
}
