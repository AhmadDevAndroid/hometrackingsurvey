package com.app.householdtracing.ui.screens

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.theme.secondaryTextColorDark
import com.app.householdtracing.ui.views.NavScreensScaffoldBackground
import com.app.householdtracing.ui.views.SmallButton
import com.app.householdtracing.util.CameraProcessor

@Composable
fun ShoppingCameraScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scannedResult by remember { mutableStateOf("") }
    val previewView = remember { PreviewView(context) }

    // Initialize Camera Processor
    val cameraProcessor = remember {
        CameraProcessor(
            context,
            lifecycleOwner,
            onBarcodeResult = { result ->
                scannedResult = "Barcode: $result"
            },
            onReceiptResult = { result ->
                scannedResult = result
            }
        )
    }

    LaunchedEffect(Unit) {
        cameraProcessor.bindCamera(previewView)
    }

    NavScreensScaffoldBackground(
        text = stringResource(R.string.tv_camera_screen_title),
        onBackPress = { onBackClick() }
    ) {

        Text(
            text = stringResource(R.string.tv_scan_product),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                lineHeight = 22.5.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HouseHoldTheme.dimens.grid_3_5)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HouseHoldTheme.dimens.grid_3_5)
                .weight(1f)
                .clip(RoundedCornerShape(HouseHoldTheme.dimens.grid_1_5))
                .border(
                    HouseHoldTheme.dimens.grid_0_5,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(HouseHoldTheme.dimens.grid_1_5)
                )

        ) {
            AndroidView(
                factory = {
                    previewView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            )
        }

        SmallButton(
            text = stringResource(R.string.btn_scan),
            onClick = { cameraProcessor.startScanning() }
        )

        Text(
            text = buildAnnotatedString {
                append("Not Found?")
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.secondary,
                        fontStyle = FontStyle.Italic
                    )
                ) {
                    append("\tSearch Manually")
                }
            },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = HouseHoldTheme.dimens.grid_2,
                    bottom = HouseHoldTheme.dimens.grid_4_5
                ),
            style = MaterialTheme.typography.titleMedium.copy(
                lineHeight = 22.sp
            ),
            color = secondaryTextColorDark,
            textAlign = TextAlign.Center
        )

    }

}