package io.github.dracula101.jetscan.presentation.features.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val ONBOARDING_ROUTE = "onboarding"

fun NavGraphBuilder.onboarding() {
    composable(ONBOARDING_ROUTE) { JetScanOnboarding() }
}

fun NavHostController.navigateToOnboarding() {
    navigate(ONBOARDING_ROUTE)
}


