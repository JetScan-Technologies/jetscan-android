package io.github.dracula101.jetscan.presentation.features.tools.split_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val SPLIT_PDF_ROUTE = "split_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createSplitPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(SPLIT_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            SplitPdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToSplitPdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(SPLIT_PDF_ROUTE, navOptions)
}