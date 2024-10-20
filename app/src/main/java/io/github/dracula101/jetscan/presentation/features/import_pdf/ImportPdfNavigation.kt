package io.github.dracula101.jetscan.presentation.features.import_pdf

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation
import java.io.File

const val IMPORT_PDF_GRAPH_ROUTE = "import_pdf_graph"
const val IMPORT_PDF_ROUTE = "import_pdf"


const val IMPORT_PDF_URI_ARGUMENT = "import_pdf_uri"
const val IMPORT_PDF_NAME_ARGUMENT = "import_pdf_name"

fun NavGraphBuilder.importPdfDestination(navController: NavHostController) {
    composableWithStayTransitions(
        route = "$IMPORT_PDF_ROUTE?$IMPORT_PDF_URI_ARGUMENT={$IMPORT_PDF_URI_ARGUMENT}&$IMPORT_PDF_NAME_ARGUMENT={$IMPORT_PDF_NAME_ARGUMENT}",
        arguments = listOf(
            navArgument(IMPORT_PDF_URI_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(IMPORT_PDF_NAME_ARGUMENT) {
                type = NavType.StringType
            }
        )
    ) {
        val tempPdfFile = File(it.arguments?.getString(IMPORT_PDF_URI_ARGUMENT) ?: "")
        val pdfNameArgument = it.arguments?.getString(IMPORT_PDF_NAME_ARGUMENT) ?: ""

        LockOrientation(
            isPortraitOnly = true
        ) {
            ImportPdfScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                tempPdfFile = tempPdfFile,
                pdfName = pdfNameArgument
            )
        }

    }
}

fun NavController.navigateToImportPdf(tempPdfFile: File, pdfName: String?, navOptions: NavOptions? = null) {
    navigate(
        "$IMPORT_PDF_ROUTE?$IMPORT_PDF_URI_ARGUMENT=$tempPdfFile&$IMPORT_PDF_NAME_ARGUMENT=$pdfName",
        navOptions
    )
}