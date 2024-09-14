package io.github.dracula101.jetscan.presentation.features.tools.merge_pdf

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation


const val MERGE_PDF_ROUTE = "merge_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createMergePdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(MERGE_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            MergePdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToMergePdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(MERGE_PDF_ROUTE, navOptions)
}
