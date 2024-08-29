package io.github.dracula101.jetscan.presentation.features.splash

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions

const val SPLASH_ROUTE = "splash_route"

/**
 * Add splash destinations to the nav graph.
 */
fun NavGraphBuilder.splashDestination() {
    composableWithStayTransitions(SPLASH_ROUTE) { SplashScreen() }
}

/**
 * Navigate to the splash screen.
 */
fun NavController.navigateToSplashRoute(
    navOptions: NavOptions? = null,
) {
    navigate(SPLASH_ROUTE, navOptions)
}
