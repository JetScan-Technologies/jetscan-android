package io.github.dracula101.jetscan.presentation.features.document.scanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithSlideTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType

const val SCANNER_ROUTE = "scanner_route"

const val DOCUMENT_TAB_TYPE = "document_tab_type"

fun NavGraphBuilder.createScannerDestination(
    onNavigateBack: () -> Unit,
    onNavigateToPdf: (id: String, name: String) -> Unit,
) {
    composableWithSlideTransitions(
        route = "$SCANNER_ROUTE/{$DOCUMENT_TAB_TYPE}",
        arguments = listOf(
            navArgument(DOCUMENT_TAB_TYPE) {
                type = NavType.StringType
            }
        )
    ){
        val startDocumentArgument = it.arguments?.getString(DOCUMENT_TAB_TYPE)
        val startDocumentTab : DocumentType? = if (startDocumentArgument != null ) DocumentType.valueOf(startDocumentArgument) else null
        LockOrientation(isPortraitOnly = true) {
            ScannerScreen(
                onNavigateBack = onNavigateBack,
                navigateToPdf = onNavigateToPdf,
                startingTab = startDocumentTab ?: DocumentType.DOCUMENT
            )
        }
    }
}
/**
 * Navigate to the home screen. Note this will only work if home destination was added
 * via [authGraph].
 */
fun NavController.navigateToScannerRoute(
    navOptions: NavOptions? = null,
    documentType: DocumentType = DocumentType.DOCUMENT
) {
    navigate(
        "$SCANNER_ROUTE/${documentType.name}",
        navOptions
    )
}
