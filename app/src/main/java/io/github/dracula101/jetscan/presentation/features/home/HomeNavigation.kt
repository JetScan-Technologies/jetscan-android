package io.github.dracula101.jetscan.presentation.features.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.features.auth.login.navigateToLoginRoute
import io.github.dracula101.jetscan.presentation.features.document.edit.createEditDocumentDestination
import io.github.dracula101.jetscan.presentation.features.document.edit.navigateToEditDocument
import io.github.dracula101.jetscan.presentation.features.document.folder.createFolderDocumentDestinationRoute
import io.github.dracula101.jetscan.presentation.features.document.folder.navigateToFolder
import io.github.dracula101.jetscan.presentation.features.document.folder.navigateToFolderDest
import io.github.dracula101.jetscan.presentation.features.document.ocr.createOcrDestination
import io.github.dracula101.jetscan.presentation.features.document.ocr.navigateToOcr
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
import io.github.dracula101.jetscan.presentation.features.settings.document.createDocumentSettingsDestination
import io.github.dracula101.jetscan.presentation.features.settings.document.navigateToDocumentSettings
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.createOpenSourceLibrariesDestination
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.navigateToOpenSourceLibraryScreen
import io.github.dracula101.jetscan.presentation.features.testers.createTesterDestination
import io.github.dracula101.jetscan.presentation.features.testers.navigateToTester
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
                when (subPage.page) {
                    MainHomeSubPage.QR_CODE -> navController.navigateToScannerRoute(documentType = DocumentType.QR_CODE)
                    MainHomeSubPage.WATERMARK -> navController.navigateToWatermarkPdfScreen(document = subPage.document)
                    MainHomeSubPage.ESIGN_PDF -> navController.navigateToESignPdfScreen(document = subPage.document)
                    MainHomeSubPage.SPLIT_PDF -> navController.navigateToSplitPdfScreen(document = subPage.document)
                    MainHomeSubPage.MERGE_PDF -> navController.navigateToMergePdfScreen(document = subPage.document)
                    MainHomeSubPage.PROTECT_PDF -> navController.navigateToProtectPdfScreen(document = subPage.document)
                    MainHomeSubPage.COMPRESS_PDF -> navController.navigateToCompressPdfScreen(document = subPage.document)
                    MainHomeSubPage.ALL_TOOLS -> {  }
                }
            },
            onNavigateToFolder = { folderId, path->
                navController.navigateToFolder(folderId, path)
            },
            navigateToAboutPage = {
                navController.navigateToAboutPage()
            },
            navigateToDocumentSettings = { screen ->
                navController.navigateToDocumentSettings(screen)
            },
            navigateToTester = {
                navController.navigateToTester()
            },
            navigateToLoginPage = {
                navController.navigateToLoginRoute(fromHome = true)
            }
        )
        createDocumentSettingsDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createPreviewDocumentDestination(
            onNavigateBack = {
                navController.navigateUp()
            },
            onNavigateToPdf = { document ->
                navController.navigateToPdfViewScreen(document = document)
            },
            onNavigateToEdit = { scannedDocument, index ->
                navController.navigateToEditDocument(scannedDocument, pageIndex = index)
            }
        )
        createEditDocumentDestination(
            onNavigateBack = {
                navController.navigateUp()
            },
            onNavigateToPdf = { document ->
                navController.navigateToPdfViewScreen(
                    documentId = document.id,
                    documentName = document.name
                )
            },
            onNavigateToOcr = { document, pageIndex ->
                navController.navigateToOcr(
                    documentId = document.id,
                    documentName = document.name,
                    pageIndex = pageIndex,
                )
            }
        )
        createOcrDestination(
            onNavigateBack = {
                navController.navigateUp()
            },
        )
        createScannerDestination(
            onNavigateBack = {
                navController.navigateUp()
            },
            onNavigateToPdf = { id, name ->
                navController.run {
                    navigateUp()
                    navigateToPdfViewScreen(
                        documentId = id,
                        documentName = name,
                    )
                }
            }
        )
        createFolderDocumentDestinationRoute(
            onNavigateBack = {
                navController.navigateUp()
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
                navController.navigateUp()
            },
            onNavigateToOpenSourceLibraries = {
                navController.navigateToOpenSourceLibraryScreen()
            }
        )
        createOpenSourceLibrariesDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createPdfViewDestination(
            onNavigateBack = {
                navController.navigateUp()
            },
            onNavigateToCompression = { document ->
                navController.navigateToCompressPdfScreen(document = document)
            },
            onNavigateToMerge = { document ->
                navController.navigateToMergePdfScreen(document = document)
            },
            onNavigateToProtect = { document ->
                navController.navigateToProtectPdfScreen(document = document)
            },
            onNavigateToSplit = { document ->
                navController.navigateToSplitPdfScreen(document = document)
            }
        )

        // TOOLS SCREENS HERE
        createCompressPdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createESignPdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createMergePdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createProtectPdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createSplitPdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createWatermarkPdfDestination(
            onNavigateBack = {
                navController.navigateUp()
            }
        )
        createTesterDestination(
            navController = navController
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
