package com.app.householdtracing.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screens {

    @Serializable
    data object LoginScreen : Screens()

    @Serializable
    data object ShoppingTripScreen : Screens()

    @Serializable
    data object ShowShoppingCameraScreen : Screens()

    @Serializable
    data object CalendarScreen : Screens()

    @Serializable
    data object StoresMapScreen : Screens()
}

