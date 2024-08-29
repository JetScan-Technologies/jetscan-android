package io.github.dracula101.jetscan.presentation.features.settings.document

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.OpenSourceLibrariesScreen
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions


const val DOCUMENT_SETTINGS_SCREEN_ROUTE = "document_settings_screen"

fun NavGraphBuilder.createDocumentSettingsDestination(
    onNavigateBack: () -> Unit
) {
    composableWithPushTransitions(DOCUMENT_SETTINGS_SCREEN_ROUTE) {
        OpenSourceLibrariesScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavHostController.navigateToDocumentSettings() {
    navigate(DOCUMENT_SETTINGS_SCREEN_ROUTE)
}

