package io.github.dracula101.jetscan.presentation.features.tools.split_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val SPLIT_PDF_ROUTE = "split_pdf_route"
const val SPLIT_PDF_ROUTE_DOCUMENT_ID_ARGS = "document_id"
/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createSplitPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("${SPLIT_PDF_ROUTE}/{$SPLIT_PDF_ROUTE_DOCUMENT_ID_ARGS}") {
        LockOrientation(
            isBoth = true
        ) {
            val documentId = it.arguments?.getString(SPLIT_PDF_ROUTE_DOCUMENT_ID_ARGS)
            SplitPdfScreen(
                onNavigateBack = onNavigateBack,
                documentId = documentId,
            )
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToSplitPdfScreen(
    document: Document? = null,
    navOptions: NavOptions? = null,
) {
    navigate("${SPLIT_PDF_ROUTE}/${document?.id}", navOptions)
}