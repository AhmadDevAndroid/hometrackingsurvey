package com.app.householdtracing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.app.householdtracing.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

)

val Typography.regular: TextStyle
    @Composable
    get() {
        return TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.regular)),
            fontSize = 14.sp
        )
    }

val Typography.medium: TextStyle
    @Composable
    get() {
        return TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.medium)),
            fontSize = 20.sp
        )
    }

val Typography.light: TextStyle
    @Composable
    get() {
        return TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.light))
        )
    }

val Typography.semi_bold: TextStyle
    @Composable
    get() {
        return TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.semi_bold))
        )
    }

val Typography.bold: TextStyle
    @Composable
    get() {
        return TextStyle(
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.bold))
        )
    }