package com.app.householdtracing.ui.views

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.householdtracing.ui.theme.primaryTextColor
import com.app.householdtracing.ui.theme.secondaryColor

@Composable
fun buttonColor(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = secondaryColor,
        disabledContainerColor = secondaryColor
            .copy(alpha = 0.2f)
        // Also contentColor and disabledContentColor
    )
}

@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    Button(
        modifier = Modifier.size(207.dp, height = 45.dp),
        onClick = {
            onClick()
        },
        enabled = isEnabled,
        colors = buttonColor()
    ) {
        Text(
            text = text,
            color = primaryTextColor
        )
    }
}