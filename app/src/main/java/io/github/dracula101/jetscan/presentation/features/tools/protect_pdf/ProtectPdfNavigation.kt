package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val PROTECT_PDF_ROUTE = "protect_pdf_route"
const val PROTECT_PDF_ROUTE_DOCUMENT_ID_ARGS = "document_id"

/**
 * Add merge pdf destinations to the nav graph.
 */
fun NavGraphBuilder.createProtectPdfDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("${PROTECT_PDF_ROUTE}/{$PROTECT_PDF_ROUTE_DOCUMENT_ID_ARGS}") {
        val documentId = it.arguments?.getString(PROTECT_PDF_ROUTE_DOCUMENT_ID_ARGS)
        LockOrientation(
            isBoth = true
        ) {
            ProtectPdfScreen(
                onNavigateBack = onNavigateBack,
                documentId = documentId,
            )
        }
    }
}

/**
 * Navigate to the merge pdf screen.
 */
fun NavController.navigateToProtectPdfScreen(
    document: Document? = null,
    navOptions: NavOptions? = null,
) {
    navigate("${PROTECT_PDF_ROUTE}/${document?.id}", navOptions)
}