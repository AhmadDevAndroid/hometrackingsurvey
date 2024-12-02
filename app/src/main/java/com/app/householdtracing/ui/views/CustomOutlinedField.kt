package com.app.householdtracing.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.householdtracing.ui.theme.hintColor
import com.app.householdtracing.ui.theme.placeholderHintColor
import com.app.householdtracing.ui.theme.textFieldBGColor
import com.app.householdtracing.ui.theme.textFieldColor

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(41.dp)
        .background(textFieldBGColor, RoundedCornerShape(100.dp)),
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        color = textFieldColor
    ),
    placeholderColor: Color = placeholderHintColor
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = hintColor,
            maxLines = 1,
            lineHeight = 14.sp,
            textAlign = TextAlign.Start
        )//Text
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = modifier,
            enabled = true,
            readOnly = false,
            singleLine = true,
            interactionSource = MutableInteractionSource(),
            maxLines = 1,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(color = placeholderColor),
                            textAlign = TextAlign.Start
                        )
                    }
                    innerTextField()
                }
            }
        )//Textfield

    }
}
