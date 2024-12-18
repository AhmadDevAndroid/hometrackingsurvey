package com.app.householdtracing.navigation


sealed class Screen(val route: String) {
    data object LoginScreen : Screen("LoginScreen")
    data object ShoppingTripScreen : Screen("ShoppingTripScreen")
    data object ShowShoppingCameraScreen : Screen("ShowShoppingCameraScreen")
}
