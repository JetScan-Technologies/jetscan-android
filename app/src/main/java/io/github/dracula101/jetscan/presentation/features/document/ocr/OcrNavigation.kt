package io.github.dracula101.jetscan.presentation.features.document.ocr

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions

const val OCR_ROUTE = "ocr"
const val OCR_DOC_ID_ARGUMENT = "document_id"
const val OCR_PAGE_INDEX_ARGUMENT = "page_index"
const val OCR_DOC_NAME_ARGUMENT = "document_name"

fun NavGraphBuilder.createOcrDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(
        route = "$OCR_ROUTE/{$OCR_DOC_ID_ARGUMENT}?$OCR_DOC_NAME_ARGUMENT={$OCR_DOC_NAME_ARGUMENT}&$OCR_PAGE_INDEX_ARGUMENT={$OCR_PAGE_INDEX_ARGUMENT}",
        arguments = listOf(
            navArgument(OCR_DOC_ID_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(OCR_DOC_NAME_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(OCR_PAGE_INDEX_ARGUMENT) {
                type = NavType.IntType
            }
        )
    ){
        val documentId = it.arguments?.getString(OCR_DOC_ID_ARGUMENT)
            ?: throw IllegalArgumentException("Missing $OCR_DOC_ID_ARGUMENT argument")
        val documentName = it.arguments?.getString(OCR_DOC_NAME_ARGUMENT)
        val pageIndex = it.arguments?.getInt(OCR_PAGE_INDEX_ARGUMENT)

        OcrScreen(
            onNavigate = onNavigateBack,
            documentId = documentId,
            documentName = documentName,
            pageIndex = pageIndex
        )
    }

}


fun NavController.navigateToOcr(
    documentId: String,
    documentName: String?,
    pageIndex: Int?,
) {
    navigate("$OCR_ROUTE/$documentId?$OCR_DOC_NAME_ARGUMENT=${documentName}&$OCR_PAGE_INDEX_ARGUMENT=${pageIndex}")
}