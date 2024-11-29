package com.app.householdtracing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.app.householdtracing.tracking.UserHouseTrackingService
import com.app.householdtracing.ui.permission.PermissionPopup
import com.app.householdtracing.ui.theme.HouseHoldTracingTheme
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.PermissionUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HouseHoldTracingTheme {
                PermissionHandler()
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//
//
//                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler() {

    val context = LocalContext.current

    val permissions = PermissionUtil.getPermissionList()
    val locationPermission = rememberPermissionState(permission = permissions[0])
    val notificationPermission = rememberPermissionState(permission = permissions[2])


    val isAllPermissionGranted =
        locationPermission.status.isGranted && notificationPermission.status.isGranted

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->

        permissionsMap.entries.forEach { (permission, isGranted) ->
            /*if (isGranted) {
                showLogError("Permissions", "$permission granted.")
            } else {
                showLogError("Permissions", "$permission denied.")
            }*/
        }
    }

    when {
        isAllPermissionGranted -> {
            showLogError("${App.APP_TAG} Permissions", "all granted")

            LaunchedEffect(key1 = Unit) {
                startLocationTrackingService(context)
            }
        }

        else -> {
            PermissionPopup(
                yesBtnText = stringResource(id = R.string.allow),
                permissionText = stringResource(id = R.string.allow_permission_msg),
                onDenyPress = {
                    (context as Activity).finish()
                },
                onAllowPress = {
                    launcherMultiplePermissions.launch(permissions)
                })
        }
    }
}

private fun startLocationTrackingService(context: Context) {
    val serviceIntent = Intent(context, UserHouseTrackingService::class.java)
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            context.startForegroundService(serviceIntent)
        }

        else -> {
            context.startService(serviceIntent)
        }
    }
}

