package io.github.dracula101.jetscan.presentation.features.settings.open_source_libs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions


const val OPEN_SOURCE_LICENSE_SCREEN_ROUTE = "open_source_license_screen"

fun NavGraphBuilder.createOpenSourceLibrariesDestination(
    onNavigateBack: () -> Unit
) {
    composableWithPushTransitions(OPEN_SOURCE_LICENSE_SCREEN_ROUTE) {
        OpenSourceLibrariesScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavHostController.navigateToOpenSourceLibraryScreen() {
    navigate(OPEN_SOURCE_LICENSE_SCREEN_ROUTE)
}


