package com.app.householdtracing.navigation


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ShoppingTrip : Screen("shopping_trip")
}
