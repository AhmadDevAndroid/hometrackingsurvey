package com.app.householdtracing.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.viewmodels.SharedViewModel
import com.app.householdtracing.ui.views.CalendarView
import com.app.householdtracing.ui.views.CustomTextFieldDate
import com.app.householdtracing.ui.views.NavScreensScaffoldBackground
import com.app.householdtracing.ui.views.SmallButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val sharedVM: SharedViewModel = koinViewModel()

    NavScreensScaffoldBackground(
        text = stringResource(R.string.tv_when_you_buy),
        onBackPress = { onBackClick() }
    ) {
        Spacer(modifier = Modifier.height(HouseHoldTheme.dimens.grid_3_5))
        CustomTextFieldDate(
            text = stringResource(R.string.tv_select_date),
            value = sharedVM.currentDate,
            onValueChange = {
                sharedVM.currentDate = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus(true)
                }
            ),
            isStoreScreen = false,
            readOnly = true
        )

        Spacer(modifier = Modifier.height(HouseHoldTheme.dimens.grid_3_5))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarView(sharedVM = sharedVM)
        }
        Spacer(modifier = Modifier.height(HouseHoldTheme.dimens.grid_3_5))

        SmallButton(
            text = stringResource(R.string.btn_next),
            onClick = { onNextClick() }
        )
    }

}
