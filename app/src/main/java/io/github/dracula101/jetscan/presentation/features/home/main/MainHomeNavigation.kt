package io.github.dracula101.jetscan.presentation.features.home.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.features.home.main.components.PdfActionPage
import io.github.dracula101.jetscan.presentation.features.settings.document.DocumentSettingScreen
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val MAIN_HOME_ROUTE = "main_home"

fun NavGraphBuilder.createMainHomeDestination(
    onNavigateToDocument: (Document) -> Unit,
    navigateToSubPage: (PdfActionPage) -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToFolder: (folderId: String, path: String) -> Unit,
    navigateToAboutPage: () -> Unit,
    navigateToTester: () -> Unit,
    navigateToDocumentSettings: (DocumentSettingScreen) -> Unit,
    navigateToLoginPage: () -> Unit
) {
    composableWithStayTransitions(
        route = MAIN_HOME_ROUTE,
    ){
        LockOrientation(
            isBoth = true
        ){
            MainHomeScreen(
                onNavigateDocument = onNavigateToDocument,
                onNavigateToScanner = onNavigateToScanner,
                navigateTo = navigateToSubPage,
                onNavigateToFolder = { folder->
                    onNavigateToFolder(folder.id, folder.path)
                },
                onNavigateToAboutPage = {
                    navigateToAboutPage()
                },
                onNavigateToDocumentSettings = { screen ->
                    navigateToDocumentSettings(screen)
                },
                onNavigateToLogin = {
                    navigateToLoginPage()
                }
            )
        }
    }
}

fun NavHostController.navigateToMainHomeRoute() {
    navigate(MAIN_HOME_ROUTE)
}
