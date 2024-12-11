package com.app.householdtracing.navigation


sealed class Screen(val route: String) {
    object LoginScreen : Screen("LoginScreen")
    object ShoppingTripScreen : Screen("ShoppingTripScreen")
}
