package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val COMPRESS_PDF_ROUTE = "compress_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createCompressPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(COMPRESS_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            CompressPdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToCompressPdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(COMPRESS_PDF_ROUTE, navOptions)
}
