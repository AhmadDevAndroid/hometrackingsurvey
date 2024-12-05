package com.app.householdtracing.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

const val ENABLE_DARK_THEME = false

private val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    secondary = secondaryColor,
    background = lightThemeBackgroundColor,
    surface = primaryTextColorDark,
    error = errorColor,
    onPrimary = primaryTextColor,
    onSecondary = secondaryTextColor,
    onBackground = lightThemeBackgroundColor,
    onSurface = primaryTextColorDark,
    onError = errorColor
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    secondary = secondaryColor,
    background = lightThemeBackgroundColor,
    surface = primaryTextColorDark,
    error = errorColor,
    onPrimary = primaryTextColor,
    onSecondary = secondaryTextColor,
    onBackground = lightThemeBackgroundColor,
    onSurface = primaryTextColorDark,
    onError = errorColor
)

@Composable
fun ProvideDimens(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalAppDimens provides dimensionSet, content = content)
}

private val LocalAppDimens = staticCompositionLocalOf {
    smallDimensions
}

@Composable
fun HouseHoldTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isPreview: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (ENABLE_DARK_THEME) {
        if (isDarkTheme) DarkColorScheme else LightColorScheme
    } else {
        LightColorScheme
    }
    val configuration = LocalConfiguration.current
    val dimensions = if (configuration.screenWidthDp <= 360) smallDimensions else sw360Dimensions

    if (!isPreview) {
        SetSystemBarsColor(color = colors.primary, isLightTheme = !isDarkTheme)
    }

    ProvideDimens(dimensions = dimensions) {
        MaterialTheme(
            colorScheme = colors,
            typography = HouseHoldTypography,
            shapes = HouseHoldShapes,
            content = content
        )
    }

}

object HouseHoldTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current
}

@SuppressLint("ObsoleteSdkInt")
@Composable
fun SetSystemBarsColor(color: Color, isLightTheme: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity

    SideEffect {
        activity?.let { act ->
            val window = act.window
            //system bar colors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = color.toArgb()
                window.navigationBarColor = color.toArgb()
            }

            // Adjust the status bar and navigation bar icons for light/dark themes
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = isLightTheme
            insetsController.isAppearanceLightNavigationBars = isLightTheme
        }
    }
}