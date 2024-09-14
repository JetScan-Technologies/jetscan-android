package io.github.dracula101.jetscan.presentation.features.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.features.document.edit.createEditDocumentDestination
import io.github.dracula101.jetscan.presentation.features.document.edit.navigateToEditDocument
import io.github.dracula101.jetscan.presentation.features.document.folder.createFolderDocumentDestinationRoute
import io.github.dracula101.jetscan.presentation.features.document.folder.navigateToFolder
import io.github.dracula101.jetscan.presentation.features.document.folder.navigateToFolderDest
import io.github.dracula101.jetscan.presentation.features.document.pdfview.createPdfViewDestination
import io.github.dracula101.jetscan.presentation.features.document.pdfview.navigateToPdfViewScreen
import io.github.dracula101.jetscan.presentation.features.document.preview.createPreviewDocumentDestination
import io.github.dracula101.jetscan.presentation.features.document.preview.navigateToPreviewDocument
import io.github.dracula101.jetscan.presentation.features.document.scanner.createScannerDestination
import io.github.dracula101.jetscan.presentation.features.document.scanner.navigateToScannerRoute
import io.github.dracula101.jetscan.presentation.features.home.main.MAIN_HOME_ROUTE
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.features.home.main.createMainHomeDestination
import io.github.dracula101.jetscan.presentation.features.settings.about.createAboutScreenDestination
import io.github.dracula101.jetscan.presentation.features.settings.about.navigateToAboutPage
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.createOpenSourceLibrariesDestination
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.navigateToOpenSourceLibraryScreen
import io.github.dracula101.jetscan.presentation.features.tools.compress_pdf.createCompressPdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.compress_pdf.navigateToCompressPdfScreen
import io.github.dracula101.jetscan.presentation.features.tools.esign_pdf.createESignPdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.esign_pdf.navigateToESignPdfScreen
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.createMergePdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.navigateToMergePdfScreen
import io.github.dracula101.jetscan.presentation.features.tools.protect_pdf.createProtectPdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.protect_pdf.navigateToProtectPdfScreen
import io.github.dracula101.jetscan.presentation.features.tools.split_pdf.createSplitPdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.split_pdf.navigateToSplitPdfScreen
import io.github.dracula101.jetscan.presentation.features.tools.watermark_pdf.createWatermarkPdfDestination
import io.github.dracula101.jetscan.presentation.features.tools.watermark_pdf.navigateToWatermarkPdfScreen
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType

const val HOME_GRAPH_ROUTE = "home_graph"

@Suppress("LongMethod")
fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation(
        startDestination = MAIN_HOME_ROUTE,
        route = HOME_GRAPH_ROUTE,
    ) {
        createMainHomeDestination(
            onNavigateToDocument = { scannedDocument ->
                navController.navigateToPreviewDocument(scannedDocument)
            },
            onNavigateToScanner = {
                navController.navigateToScannerRoute()
            },
            navigateToSubPage = { subPage ->
                when (subPage) {
                    MainHomeSubPage.QR_CODE -> navController.navigateToScannerRoute(documentType = DocumentType.QR_CODE)
                    MainHomeSubPage.WATERMARK -> navController.navigateToWatermarkPdfScreen()
                    MainHomeSubPage.ESIGN_PDF -> navController.navigateToESignPdfScreen()
                    MainHomeSubPage.SPLIT_PDF -> navController.navigateToSplitPdfScreen()
                    MainHomeSubPage.MERGE_PDF -> navController.navigateToMergePdfScreen()
                    MainHomeSubPage.PROTECT_PDF -> navController.navigateToProtectPdfScreen()
                    MainHomeSubPage.COMPRESS_PDF -> navController.navigateToCompressPdfScreen()
                    MainHomeSubPage.ALL_TOOLS -> TODO()
                }
            },
            onNavigateToFolder = { folderId, path->
                navController.navigateToFolder(folderId, path)
            },
            navigateToAboutPage = {
                navController.navigateToAboutPage()
            }
        )
        createPreviewDocumentDestination(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToEdit = { scannedDocument, index ->
                navController.navigateToEditDocument(scannedDocument, pageIndex = index)
            }
        )
        createEditDocumentDestination(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPdf = { document ->
                navController.navigateToPdfViewScreen(document)
            }
        )
        createScannerDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createFolderDocumentDestinationRoute(
            onNavigateBack = {
                navController.popBackStack()
            },
            navigateToDoc = { document ->
                navController.navigateToEditDocument(document, pageIndex = 0)
            },
            navigateToFolder = { folder->
                navController.navigateToFolder(folder.id, folder.path)
            },
            navigateToFolderPath = { folderName, folderDocId, path->
                navController.navigateToFolderDest(folderName, folderDocId, path)
            }
        )
        createAboutScreenDestination(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToOpenSourceLibraries = {
                navController.navigateToOpenSourceLibraryScreen()
            }
        )
        createOpenSourceLibrariesDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createPdfViewDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )

        // TOOLS SCREENS HERE
        createCompressPdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createESignPdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createMergePdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createProtectPdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createSplitPdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
        createWatermarkPdfDestination(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

/**
 * Navigate to the home screen. Note this will only work if home destination was added
 * via [authGraph].
 */
fun NavController.navigateToHomeGraph(
    navOptions: NavOptions? = null,
) {
    navigate(HOME_GRAPH_ROUTE, navOptions)
}
