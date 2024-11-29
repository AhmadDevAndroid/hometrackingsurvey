package com.app.householdtracing.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.householdtracing.ui.theme.Typography
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.medium
import com.app.householdtracing.ui.theme.regular

@Composable
inline fun PermissionPopup(
    yesBtnText: String,
    permissionText: String,
    crossinline onDenyPress: () -> Unit,
    crossinline onAllowPress: () -> Unit,
) {

        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false, dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(15.dp))
                    .background(colorResource(id = R.color.white))
                    .wrapContentWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 30.dp),
                    text = stringResource(id = R.string.allow_permission),
                    color = colorResource(id = R.color.black),
                    style = Typography.medium
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(10.dp),
                    text = permissionText,
                    color = colorResource(id = R.color.black),
                    style = Typography.regular
                )

                Row(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {


                    PermissionButton(
                        colorResource(id = R.color.purple_500),
                        stringResource(R.string.not_now),
                        onClick = {
                            onDenyPress()
                        })

                    PermissionButton(
                        colorResource(id = R.color.purple_700),
                        yesBtnText,
                        onClick = {
                            onAllowPress()
                        })
                }

            }
    }
}

@Composable
inline fun RowScope.PermissionButton(color: Color, text: String, crossinline onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        modifier = Modifier
            .padding(0.dp)
            .weight(1f, true)
            .heightIn(48.dp),
        shape = RoundedCornerShape(0.dp),
        onClick = {
            onClick()
        }) {
        Text(
            text = text,
            color = colorResource(id = R.color.white),
            style = Typography.bodyMedium,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}