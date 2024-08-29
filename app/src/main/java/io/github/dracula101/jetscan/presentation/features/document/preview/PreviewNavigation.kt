package io.github.dracula101.jetscan.presentation.features.document.preview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation


const val PREVIEW_DOCUMENT_ROUTE = "preview_document"
const val PREVIEW_DOCUMENT_NAME_ARGUMENT = "document_name"

fun NavGraphBuilder.createPreviewDocumentDestination(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Document, Int) -> Unit
) {
    composableWithPushTransitions(
        route = "$PREVIEW_DOCUMENT_ROUTE/{$PREVIEW_DOCUMENT_NAME_ARGUMENT}",
        arguments = listOf(
            navArgument(PREVIEW_DOCUMENT_NAME_ARGUMENT) {
                type = NavType.StringType
            }
        )
    ){
        val documentName = it.arguments?.getString(PREVIEW_DOCUMENT_NAME_ARGUMENT)
        LockOrientation (isBoth = true){
            PreviewScreen(
                documentId = documentName,
                onBack = onNavigateBack,
                onEdit = onNavigateToEdit
            )
        }
    }
}
/**
 * Navigate to the home screen. Note this will only work if home destination was added
 * via [authGraph].
 */
fun NavController.navigateToPreviewDocument(
    scannedDocument: Document?,
    navOptions: NavOptions? = null,
) {
    navigate("$PREVIEW_DOCUMENT_ROUTE/${scannedDocument?.id}", navOptions)
}
