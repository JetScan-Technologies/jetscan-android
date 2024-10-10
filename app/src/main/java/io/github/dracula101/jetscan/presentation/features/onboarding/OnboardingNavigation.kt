package io.github.dracula101.jetscan.presentation.features.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import io.github.dracula101.jetscan.presentation.features.auth.navigateToAuthGraph
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithSlideTransitions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions

const val INITIAL_STARTING_ROUTE = "initial_starting"
const val ONBOARDING_ROUTE = "onboarding"
const val ONBOARDING_GRAPH_ROUTE = "onboarding_graph"

fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
) {
    navigation(
        startDestination = INITIAL_STARTING_ROUTE,
        route = ONBOARDING_GRAPH_ROUTE
    ){
        composableWithStayTransitions(INITIAL_STARTING_ROUTE) {
            InitialStartingScreen(
                navigateToOnboarding = {
                    navController.navigate(ONBOARDING_ROUTE) {
                        popUpTo(INITIAL_STARTING_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composableWithSlideTransitions(ONBOARDING_ROUTE) {
            JetScanOnboarding(
                navigateToAuth = { navController.navigateToAuthGraph() }
            )
        }
    }
}


fun NavHostController.navigateToOnboarding(replace: Boolean = true) {
    val currentRoute = currentDestination?.route
    navigate(ONBOARDING_GRAPH_ROUTE){
        if (replace) {
            currentRoute?.let {
                popUpTo(it) {
                    inclusive = true
                }
            }
        }
    }
}


