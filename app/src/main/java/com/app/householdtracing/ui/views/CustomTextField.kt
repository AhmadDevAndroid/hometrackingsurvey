package com.app.householdtracing.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.theme.hintColor
import com.app.householdtracing.ui.theme.placeholderHintColor
import com.app.householdtracing.ui.theme.primaryLightColor
import com.app.householdtracing.ui.theme.textFieldColor

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(41.dp)
        .background(primaryLightColor, RoundedCornerShape(18.dp)),
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        lineHeight = 14.sp,
        color = textFieldColor
    ),
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Email
    ),
    keyboardActions: KeyboardActions = KeyboardActions(),
    //visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(true) }

    val visualTransformation = if (isPassword && passwordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HouseHoldTheme.dimens.grid_4)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 15.sp,
                lineHeight = 22.5.sp,
                textAlign = TextAlign.Start,
                color = hintColor
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HouseHoldTheme.dimens.grid_2)

        )//Text
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(HouseHoldTheme.dimens.grid_1)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        keyboardController?.hide()
                        true
                    } else {
                        false
                    }
                },
            enabled = true,
            readOnly = false,
            visualTransformation = visualTransformation,
            singleLine = true,
            interactionSource = MutableInteractionSource(),
            maxLines = 1,
            cursorBrush = SolidColor(textFieldColor),
            decorationBox = @Composable { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HouseHoldTheme.dimens.grid_2),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 14.sp,
                                color = placeholderHintColor,
                                textAlign = TextAlign.Start
                            ),
                            maxLines = 1
                        )
                    }
                    if (isPassword) {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier
                                .align(Alignment.CenterEnd),
                            interactionSource = MutableInteractionSource(),
                        ) {
                            Icon(
                                painter = painterResource(if (!passwordVisible) R.drawable.ic_password_visible else R.drawable.ic_password_hide),
                                contentDescription = null
                            )
                        }
                    }

                    innerTextField()
                }

            },
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions
        )//Textfield

    }
}
