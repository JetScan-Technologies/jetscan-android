package io.github.dracula101.jetscan.presentation.features.testers

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithRootPushTransitions

const val TESTER_START_ROUTE = "tester"
const val TESTER_FEATURE_ROUTE = "tester_feature"

fun NavGraphBuilder.createTesterDestination(
    navController: NavController,
) {
    composableWithRootPushTransitions(
        route = TESTER_START_ROUTE,
    ) {
        TesterStartScreen(
            navigateToNextScreen = {
                navController.navigate(TESTER_FEATURE_ROUTE){
                    popUpTo(TESTER_START_ROUTE){
                        inclusive = true
                    }
                }
            }
        )
    }
    composableWithPushTransitions(
        route = TESTER_FEATURE_ROUTE,
    ) {
        TesterFeatureScreen(
            onBackNavigation = {
                navController.navigateUp()
            }
        )
    }
}

fun NavController.navigateToTester() {
    navigate(TESTER_START_ROUTE)
}