package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val PROTECT_PDF_ROUTE = "protect_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createProtectPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(PROTECT_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            ProtectPdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToProtectPdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(PROTECT_PDF_ROUTE, navOptions)
}