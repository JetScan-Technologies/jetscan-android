package io.github.dracula101.jetscan.presentation.features.document.pdfview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val PDF_VIEW_ROUTE = "pdf_view_route"
const val PDF_VIEW_DOCUMENT_ID = "document_id"
const val PDF_VIEW_DOCUMENT_NAME = "document_name"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createPdfViewDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("$PDF_VIEW_ROUTE/{$PDF_VIEW_DOCUMENT_ID}?${PDF_VIEW_DOCUMENT_NAME}={$PDF_VIEW_DOCUMENT_NAME}") {
        LockOrientation(
            isBoth = true
        ) {
            val documentId = it.arguments?.getString(PDF_VIEW_DOCUMENT_ID)
            val documentName = it.arguments?.getString(PDF_VIEW_DOCUMENT_NAME)
            if (documentId == null) {
                onNavigateBack()
                return@LockOrientation
            }else {
                PdfViewScreen(
                    documentId = documentId,
                    documentName = documentName,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToPdfViewScreen(
    document: Document?,
    navOptions: NavOptions? = null,
) {
    navigate(
        "$PDF_VIEW_ROUTE/${document?.id}?${PDF_VIEW_DOCUMENT_NAME}=${document?.name}",
        navOptions
    )
}
