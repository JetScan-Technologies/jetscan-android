package io.github.dracula101.jetscan.presentation.features.tools.watermark_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val WATERMARK_PDF_ROUTE = "watermark_pdf_route"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createWatermarkPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(WATERMARK_PDF_ROUTE) {
        LockOrientation(
            isBoth = true
        ) {
            WatermarkPdfScreen(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToWatermarkPdfScreen(
    navOptions: NavOptions? = null,
) {
    navigate(WATERMARK_PDF_ROUTE, navOptions)
}