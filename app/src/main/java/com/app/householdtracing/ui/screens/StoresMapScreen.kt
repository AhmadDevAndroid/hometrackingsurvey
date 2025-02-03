package com.app.householdtracing.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.viewmodels.SharedViewModel
import com.app.householdtracing.ui.views.CustomTextFieldDate
import com.app.householdtracing.ui.views.NavScreensScaffoldBackground
import com.app.householdtracing.ui.views.SmallButton
import com.app.householdtracing.util.PermissionUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun StoresMapScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val sharedVM: SharedViewModel = koinViewModel()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    PermissionUtil.PermissionHandler(
        onPermissionsGranted = {
            hasPermission = true
        },
        onPermissionsDenied = {
            Toast.makeText(context, "Permissions Denied", Toast.LENGTH_SHORT).show()
        }
    )

    NavScreensScaffoldBackground(
        text = stringResource(R.string.tv_where_you_buy),
        onBackPress = { onBackClick() }
    ) {
        Spacer(modifier = Modifier.height(HouseHoldTheme.dimens.grid_3_5))
        CustomTextFieldDate(
            text = stringResource(R.string.tv_shop_store),
            value = sharedVM.search,
            onValueChange = {
                sharedVM.search = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                keyboardType = KeyboardType.Text
            ),
            placeholder = stringResource(R.string.tv_search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus(true)
                }
            ),
            isStoreScreen = true,
            readOnly = false
        )
        Text(
            text = stringResource(R.string.tv_select_through_map),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                lineHeight = 22.5.sp,
                textAlign = TextAlign.Start
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = HouseHoldTheme.dimens.grid_2,
                    start = HouseHoldTheme.dimens.grid_3_5
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = HouseHoldTheme.dimens.grid_2,
                    horizontal = HouseHoldTheme.dimens.grid_3_5
                )
                .weight(1f)
                .clip(RoundedCornerShape(HouseHoldTheme.dimens.grid_1_5))
                .border(
                    HouseHoldTheme.dimens.grid_0_5,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(HouseHoldTheme.dimens.grid_1_5)
                )

        ) {
            if (hasPermission) {
                ShowGoogleMap()
            }
        }
        SmallButton(
            text = stringResource(R.string.btn_next),
            onClick = { onNextClick() }
        )

        Spacer(modifier = Modifier.padding(bottom = HouseHoldTheme.dimens.grid_4_5))
    }

}


@SuppressLint("MissingPermission")
@Composable
fun ShowGoogleMap() {
    val context = LocalContext.current
    val fusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                userLocation = LatLng(it.latitude, it.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 14f)
            }
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    )
}