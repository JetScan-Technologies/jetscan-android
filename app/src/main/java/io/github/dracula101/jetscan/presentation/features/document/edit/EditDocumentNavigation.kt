package io.github.dracula101.jetscan.presentation.features.document.edit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation


const val EDIT_DOC_ROUTE = "edit_doc"
const val EDIT_DOC_ID_ARGUMENT = "document_id"
const val EDIT_DOC_PAGE_INDEX_ARGUMENT = "page_index"

fun NavGraphBuilder.createEditDocumentDestination(
    onNavigateBack: () -> Unit,
    onNavigateToPdf: (Document) -> Unit,
) {
    composableWithPushTransitions(
        route = "$EDIT_DOC_ROUTE/{$EDIT_DOC_ID_ARGUMENT}/{$EDIT_DOC_PAGE_INDEX_ARGUMENT}",
        arguments = listOf(
            navArgument(EDIT_DOC_ID_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(EDIT_DOC_PAGE_INDEX_ARGUMENT) {
                type = NavType.IntType
            }
        )
    ) {
        val documentName = it.arguments?.getString(EDIT_DOC_ID_ARGUMENT)
            ?: throw IllegalArgumentException("Missing $EDIT_DOC_ID_ARGUMENT argument")
        val pageIndex = it.arguments?.getInt(EDIT_DOC_PAGE_INDEX_ARGUMENT)
            ?: throw IllegalArgumentException("Missing $EDIT_DOC_PAGE_INDEX_ARGUMENT argument")

        LockOrientation(isBoth = true) {
            EditDocumentScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToPdf = onNavigateToPdf,
                documentId = documentName,
                documentPageIndex = pageIndex,
            )
        }
    }
}

fun NavHostController.navigateToEditDocument(
    scannedDocument: Document,
    pageIndex: Int,
    navOptions: NavOptions? = null,
) {
    navigate("$EDIT_DOC_ROUTE/${scannedDocument.id}/$pageIndex", navOptions)
}
