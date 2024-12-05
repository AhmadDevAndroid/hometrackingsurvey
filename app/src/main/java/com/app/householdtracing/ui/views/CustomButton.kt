package com.app.householdtracing.ui.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun buttonColor(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        disabledContainerColor = MaterialTheme.colorScheme.secondary
            .copy(alpha = 0.2f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary
            .copy(alpha = 0.2f)
    )
}

@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.W600,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center
    )
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
            color = textColor,
            style = textStyle
        )
    }
}