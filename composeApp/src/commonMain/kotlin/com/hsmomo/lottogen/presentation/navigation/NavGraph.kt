package com.hsmomo.lottogen.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hsmomo.lottogen.presentation.home.HomeScreen
import com.hsmomo.lottogen.presentation.splash.SplashScreen

object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
}

@Composable
fun LottoNavGraph(
    navController: NavHostController,
    onShowMessage: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(route = NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onShowError = onShowMessage
            )
        }

        composable(route = NavRoutes.HOME) {
            HomeScreen(onShowMessage = onShowMessage)
        }
    }
}