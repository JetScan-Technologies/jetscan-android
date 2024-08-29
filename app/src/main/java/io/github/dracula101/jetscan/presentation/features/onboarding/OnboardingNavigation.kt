package io.github.dracula101.jetscan.presentation.features.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.dracula101.jetscan.presentation.features.splash.SPLASH_ROUTE
import io.github.dracula101.jetscan.presentation.features.splash.SplashScreen

const val ONBOARDING_ROUTE = "onboarding"

fun NavGraphBuilder.onboarding() {
    composable(ONBOARDING_ROUTE) { JetScanOnboarding() }
}

fun NavHostController.navigateToOnboarding() {
    navigate(ONBOARDING_ROUTE)
}


