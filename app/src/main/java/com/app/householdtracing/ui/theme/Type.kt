package com.app.householdtracing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.householdtracing.R

// Set of Material typography styles to start with
val householdFontFamily =
    FontFamily(
        Font(R.font.bold, weight = FontWeight.Bold),
        Font(R.font.light, weight = FontWeight.Light),
        Font(R.font.medium, weight = FontWeight.Medium),
        Font(R.font.semi_bold, weight = FontWeight.SemiBold),
        Font(R.font.regular)
    )

val HouseHoldTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    displayLarge = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp
    ),
    displayMedium = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp
    ),
    displaySmall = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    titleMedium = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    titleSmall = TextStyle(
        fontFamily = householdFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 10.sp
    )
)