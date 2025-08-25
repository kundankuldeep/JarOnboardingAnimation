package com.app.jaronboardinganimation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.jaronboardinganimation.ui.screens.dashboard.DashboardScreen
import com.app.jaronboardinganimation.ui.screens.onboarding.OnboardingScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        composable(
            route = Routes.ONBOARDING,
            exitTransition = {
                // Slide up animation when leaving onboarding
                slideOutVertically(
                    targetOffsetY = { -it }, // Slide to negative Y (upward)
                    animationSpec = tween(500)
                )
            }
        ) {
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Clear the onboarding screen from back stack
                        popUpTo(Routes.ONBOARDING) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.DASHBOARD,
            // we can use any transition here for dashboard screen
        ) {
            DashboardScreen()
        }
    }
}
