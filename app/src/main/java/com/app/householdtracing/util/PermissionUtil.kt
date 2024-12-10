package com.app.householdtracing.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.app.householdtracing.R
import com.app.householdtracing.ui.permission.PermissionPopup
import com.app.householdtracing.util.AppUtil.showLogError
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

object PermissionUtil {

    private val activityPermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            Manifest.permission.ACTIVITY_RECOGNITION
        }

        else -> {
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        }
    }

    val backgroundPermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }

        else -> {
            "android.permission.ACCESS_BACKGROUND_LOCATION"
        }
    }


    @SuppressLint("InlinedApi")
    fun getPermissionList() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        activityPermission,
        Manifest.permission.POST_NOTIFICATIONS
    )


    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun isActivityRecognitionPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            activityPermission
        ) == PackageManager.PERMISSION_GRANTED

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionHandler(
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        val permissions = getPermissionList()
        val locationPermission = rememberPermissionState(permission = permissions[0])
        val recognitionPermission = rememberPermissionState(permission = permissions[1])
        val notificationPermission = rememberPermissionState(permission = permissions[2])
        val backgroundPermission =
            rememberPermissionState(permission = PermissionUtil.backgroundPermission)

        val isAllPermissionGranted =
            locationPermission.status.isGranted && recognitionPermission.status.isGranted && notificationPermission.status.isGranted
        val isBackgroundLocationGranted = backgroundPermission.status.isGranted

        val launcherMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            permissionsMap.forEach { (permission, isGranted) ->
                showLogError(
                    "Permissions",
                    "$permission ${if (isGranted) "granted" else "denied"}."
                )
            }
        }

        when {
            isAllPermissionGranted && isBackgroundLocationGranted -> {
                onPermissionsGranted()
            }

            isAllPermissionGranted && !isBackgroundLocationGranted -> {
                PermissionPopup(
                    yesBtnText = stringResource(id = R.string.allow),
                    permissionText = stringResource(id = R.string.allow_background_permission_msg),
                    onDenyPress = { onPermissionsDenied() },
                    onAllowPress = { backgroundPermission.launchPermissionRequest() }
                )
            }

            else -> {
                PermissionPopup(
                    yesBtnText = stringResource(id = R.string.allow),
                    permissionText = stringResource(id = R.string.allow_permission_msg),
                    onDenyPress = { onPermissionsDenied() },
                    onAllowPress = { launcherMultiplePermissions.launch(permissions) }
                )
            }
        }
    }


}