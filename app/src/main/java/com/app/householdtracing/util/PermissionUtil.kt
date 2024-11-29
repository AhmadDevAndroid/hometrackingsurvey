package com.app.householdtracing.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtil {

    private val activityPermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            Manifest.permission.ACTIVITY_RECOGNITION
        }
        else -> {
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        }
    }


    @SuppressLint("InlinedApi")
    fun getPermissionList() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        activityPermission,
        Manifest.permission.POST_NOTIFICATIONS
    )


    fun isPermissionGranted(context: Context, permission: String) = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED


}