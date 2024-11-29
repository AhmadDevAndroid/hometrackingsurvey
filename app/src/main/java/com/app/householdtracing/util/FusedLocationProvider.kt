package com.app.householdtracing.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class FusedLocationProvider(private val appContext: Context) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            appContext
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? =
        withContext(Dispatchers.IO) {
            when {
                PermissionUtil.isPermissionGranted(
                    appContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    return@withContext fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
                    ).await()
                }
                else -> return@withContext null
            }
        }

}