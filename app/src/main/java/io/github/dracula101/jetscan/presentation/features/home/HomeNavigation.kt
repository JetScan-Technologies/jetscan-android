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
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType
import timber.log.Timber

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
                    MainHomeSubPage.WATERMARK -> TODO()
                    MainHomeSubPage.ESIGN_PDF -> TODO()
                    MainHomeSubPage.SPLIT_PDF -> TODO()
                    MainHomeSubPage.MERGE_PDF -> TODO()
                    MainHomeSubPage.PROTECT_PDF -> TODO()
                    MainHomeSubPage.COMPRESS_PDF -> TODO()
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
