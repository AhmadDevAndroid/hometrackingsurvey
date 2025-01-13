package com.app.householdtracing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.householdtracing.data.model.responsedto.LoginResponseBody
import com.app.householdtracing.navigation.Screens
import com.app.householdtracing.ui.screens.LoginScreen
import com.app.householdtracing.ui.screens.ShoppingCameraScreen
import com.app.householdtracing.ui.screens.ShoppingTripScreen
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.viewmodels.LoginScreenViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HouseHoldTheme {
                NavigationHandler()
            }
        }
    }
}

@Composable
fun NavigationHandler() {
    val navController = rememberNavController()
    val loginScreenViewModel: LoginScreenViewModel = koinViewModel()
    val loginState by loginScreenViewModel.getUser()
        .collectAsState(initial = LoginResponseBody(token = "initial"))

    val startDestination = remember(loginState) {
        when {
            loginState.token == "initial" -> null
            loginState.token.isNotEmpty() -> Screens.ShoppingTripScreen
            else -> Screens.LoginScreen
        }
    }

    if (startDestination == null) {
        CircularProgressIndicator()
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination//Screens.ShowShoppingCameraScreen
    ) {
        composable<Screens.LoginScreen> {
            LoginScreen(onLoginClick = {
                navController.navigate(Screens.ShoppingTripScreen) {
                    launchSingleTop = true
                    popUpTo(Screens.LoginScreen) { inclusive = true }
                }

            })
        }
        composable<Screens.ShoppingTripScreen> {
            ShoppingTripScreen(
                onGrocerMissionClick = {
                    navController.navigate(Screens.ShowShoppingCameraScreen)
                },
                onTopUpMissionClick = {},
                onImpulseBuyingMissionClick = {}
            )
        }

        composable<Screens.ShowShoppingCameraScreen> {
            ShoppingCameraScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}



