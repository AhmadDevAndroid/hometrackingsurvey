package com.app.householdtracing.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.householdtracing.App
import com.app.householdtracing.R
import com.app.householdtracing.ui.permission.PermissionPopup
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.theme.primaryDarkColor
import com.app.householdtracing.ui.viewmodels.LoginScreenViewModel
import com.app.householdtracing.ui.viewmodels.LoginUiState
import com.app.householdtracing.ui.views.CustomTextField
import com.app.householdtracing.ui.views.SmallButton
import com.app.householdtracing.util.AppUtil.showLogError
import com.app.householdtracing.util.PermissionUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
) {

    val loginScreenVM: LoginScreenViewModel = koinViewModel()
    val loginState = loginScreenVM.authState
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (!loginScreenVM.isAllPermissionGranted) {
        PermissionHandler()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSurface)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.Crop,
                painter = painterResource(id = R.drawable.logincurvedbg),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(bottom = HouseHoldTheme.dimens.grid_3)
                    .zIndex(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_login_icon),
                        contentDescription = null
                    )//image
                    Text(
                        text = stringResource(R.string.tv_track_shopping_mission),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 30.sp,
                            color = primaryDarkColor,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = HouseHoldTheme.dimens.grid_4)
                    )//text

                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(HouseHoldTheme.dimens.grid_4_5 * 2 + HouseHoldTheme.dimens.plane_3)
                    )

                    CustomTextField(
                        text = stringResource(R.string.tv_username),
                        value = loginScreenVM.emailTextField,
                        placeholder = stringResource(R.string.tv_username_placeholder),
                        onValueChange = {
                            loginScreenVM.emailTextField = it
                        },
                        keyboardActions = KeyboardActions(
                            onNext = {
                                keyboardController?.hide()
                                focusManager.moveFocus(focusDirection = FocusDirection.Down)
                            }
                        )
                    )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(HouseHoldTheme.dimens.grid_3_5)
                    )

                    CustomTextField(
                        text = stringResource(R.string.tv_password),
                        value = loginScreenVM.passwordTextField,
                        placeholder = stringResource(R.string.tv_password_placeholder),
                        isPassword = true,
                        onValueChange = {
                            loginScreenVM.passwordTextField = it
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus(true)
                            }
                        )
                    )

                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(HouseHoldTheme.dimens.grid_4 * 2)
                    )

                    SmallButton(
                        text = stringResource(R.string.btn_login),
                        onClick = {
                            if (loginScreenVM.isAllPermissionGranted) {
                                loginScreenVM.authenticate(
                                    loginScreenVM.emailTextField,
                                    loginScreenVM.passwordTextField
                                )
                            } else {
                                loginScreenVM.isAllPermissionGranted = false
                            }

                        }
                    )//login button
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp))

                    when (loginState) {
                        is LoginUiState.Idle -> Text("Please Enter Login credentials...")
                        is LoginUiState.Loading -> CircularProgressIndicator()
                        is LoginUiState.Success -> {
                            onLoginClick()
                        }
                        is LoginUiState.Error -> Text("Error: ${loginState.error}", color = Color.Red)
                    }
                }

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.None,
                    painter = painterResource(id = R.drawable.ic_company_logo),
                    contentDescription = null
                )
            }

        }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler() {
    val loginScreenVM: LoginScreenViewModel = koinViewModel()
    val context = LocalContext.current

    val permissions = PermissionUtil.getPermissionList()
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

        permissionsMap.entries.forEach { (permission, isGranted) ->
            /*if (isGranted) {
                showLogError("Permissions", "$permission granted.")
            } else {
                showLogError("Permissions", "$permission denied.")
            }*/
        }
    }


    when {
        isAllPermissionGranted && isBackgroundLocationGranted -> {
            showLogError("${App.APP_TAG} Permissions", "all granted")
            loginScreenVM.isAllPermissionGranted = true
        }

        isAllPermissionGranted && !isBackgroundLocationGranted -> {
            PermissionPopup(
                yesBtnText = stringResource(id = R.string.allow),
                permissionText = stringResource(id = R.string.allow_background_permission_msg),
                onDenyPress = {
                    (context as Activity).finish()
                },
                onAllowPress = {
                    backgroundPermission.launchPermissionRequest()
                }
            )
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