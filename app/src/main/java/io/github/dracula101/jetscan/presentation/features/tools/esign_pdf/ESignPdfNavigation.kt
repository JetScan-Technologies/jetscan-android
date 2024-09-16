package io.github.dracula101.jetscan.presentation.features.tools.esign_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val ESIGN_PDF_ROUTE = "esign_pdf_route"
const val ESIGN_PDF_ROUTE_DOCUMENT_ID_ARGS = "document_id"
/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createESignPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("${ESIGN_PDF_ROUTE}/{$ESIGN_PDF_ROUTE_DOCUMENT_ID_ARGS}") {
        val documentId = it.arguments?.getString(ESIGN_PDF_ROUTE_DOCUMENT_ID_ARGS)
        LockOrientation(
            isBoth = true
        ) {
            ESignPdfScreen(
                onNavigateBack = onNavigateBack,
                documentId = documentId,
            )
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToESignPdfScreen(
    navOptions: NavOptions? = null,
    document: Document? = null,
) {
    navigate("${ESIGN_PDF_ROUTE}/${document?.id}", navOptions)
}