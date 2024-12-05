package com.app.householdtracing.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.householdtracing.R
import com.app.householdtracing.tracking.UserHouseTrackingService
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.views.HouseHoldScaffoldBackground

@Composable
fun ShoppingTripScreen(
    onGrocerMissionClick: () -> Unit,
    onTopUpMissionClick: () -> Unit,
    onImpulseBuyingMissionClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        startLocationTrackingService(context)
    }

    HouseHoldScaffoldBackground {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(HouseHoldTheme.dimens.grid_3)
        )
        TripItem(
            onClick = { onGrocerMissionClick() },
            text = stringResource(R.string.tv_groceries),
            icon = painterResource(R.drawable.ic_groceries)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(HouseHoldTheme.dimens.grid_3)
        )
        TripItem(
            onClick = { onTopUpMissionClick() },
            text = stringResource(R.string.tv_top_up),
            icon = painterResource(R.drawable.ic_top_up)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(HouseHoldTheme.dimens.grid_3)
        )
        TripItem(
            onClick = { onImpulseBuyingMissionClick() },
            text = stringResource(R.string.tv_impulse_purchase),
            icon = painterResource(R.drawable.ic_groceries)
        )

    }
}


@Composable
fun TripItem(
    onClick: () -> Unit,
    text: String,
    icon: Painter
) {
    Box(
        modifier = Modifier
            .size(width = 141.09.dp, height = 196.21.dp)
            .clickable {
                onClick()
            }
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Crop,
            painter = icon,
            contentDescription = null
        )
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.W600,
                fontSize = 19.33.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    MaterialTheme.colorScheme.secondary,
                    RoundedCornerShape(HouseHoldTheme.dimens.grid_1)
                )
                .padding(top = HouseHoldTheme.dimens.grid_2, bottom = HouseHoldTheme.dimens.grid_1)
        )

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