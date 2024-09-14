package io.github.dracula101.jetscan.presentation.features.tools.esign_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val ESIGN_PDF_ROUTE = "esign_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createESignPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(ESIGN_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            ESignPdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToESignPdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(ESIGN_PDF_ROUTE, navOptions)
}