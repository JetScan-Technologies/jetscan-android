package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val COMPRESS_PDF_ROUTE = "compress_pdf_route"
const val COMPRESS_ROUTE_DOCUMENT_ID_ARGS = "document_id"
/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createCompressPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("${COMPRESS_PDF_ROUTE}/{$COMPRESS_ROUTE_DOCUMENT_ID_ARGS}") {
        val documentId = it.arguments?.getString(COMPRESS_ROUTE_DOCUMENT_ID_ARGS)
        LockOrientation(
            isBoth = true
        ) {
            CompressPdfScreen(
                onNavigateBack = onNavigateBack,
                documentId = documentId,
            )
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToCompressPdfScreen(
    navOptions: NavOptions? = null,
    document: Document? = null,
) {
    navigate("${COMPRESS_PDF_ROUTE}/${document?.id}", navOptions)
}
