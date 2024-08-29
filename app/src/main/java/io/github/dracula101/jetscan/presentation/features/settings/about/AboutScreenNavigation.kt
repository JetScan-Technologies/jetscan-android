package io.github.dracula101.jetscan.presentation.features.settings.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions

const val ABOUT_SCREEN_ROUTE = "about_screen"

fun NavGraphBuilder.createAboutScreenDestination(
    onNavigateBack: () -> Unit,
    onNavigateToOpenSourceLibraries: () -> Unit
) {
    composableWithPushTransitions(ABOUT_SCREEN_ROUTE) {
        AboutScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToOpenSourceLibraries = onNavigateToOpenSourceLibraries
        )
    }
}

fun NavHostController.navigateToAboutPage() {
    navigate(ABOUT_SCREEN_ROUTE)
}


