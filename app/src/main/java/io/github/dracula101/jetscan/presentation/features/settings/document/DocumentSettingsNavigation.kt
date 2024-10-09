package io.github.dracula101.jetscan.presentation.features.settings.document

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.github.dracula101.jetscan.presentation.features.settings.open_source_libs.OpenSourceLibrariesScreen
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions

const val DOCUMENT_SETTINGS_SCREEN_ROUTE = "document_settings_screen"
const val DOCUMENT_SETTINGS_ARGUMENT = "screen_type"

fun NavGraphBuilder.createDocumentSettingsDestination(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions("${DOCUMENT_SETTINGS_SCREEN_ROUTE}/{$DOCUMENT_SETTINGS_ARGUMENT}") { backStackEntry ->
        val screenType = backStackEntry.arguments?.getString(DOCUMENT_SETTINGS_ARGUMENT)
        val documentSettingScreen = DocumentSettingScreen.entries.firstOrNull { it.toRoute() == screenType } ?: DocumentSettingScreen.IMPORT_EXPORT
        DocumentSettingsScreen(
            screen = documentSettingScreen,
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavHostController.navigateToDocumentSettings(
    screen: DocumentSettingScreen
) {
    navigate("${DOCUMENT_SETTINGS_SCREEN_ROUTE}/${screen.toRoute()}")
}

enum class DocumentSettingScreen {
    IMPORT_EXPORT,
    PDF_SETTINGS,
    DOC_CONFIG,
    CAMERA_CONFIG;

    override fun toString(): String {
        return when (this) {
            IMPORT_EXPORT -> "Import/Export"
            PDF_SETTINGS -> "PDF Settings"
            DOC_CONFIG -> "Document Configuration"
            CAMERA_CONFIG -> "Camera Configuration"
        }
    }

    fun toRoute(): String {
        return when (this) {
            IMPORT_EXPORT -> "import_export"
            PDF_SETTINGS -> "pdf_settings"
            DOC_CONFIG -> "doc_config"
            CAMERA_CONFIG -> "camera_config"
        }
    }

    fun toIcon(): ImageVector {
        return when (this) {
            IMPORT_EXPORT -> Icons.Rounded.ImportExport
            PDF_SETTINGS -> Icons.Outlined.PictureAsPdf
            DOC_CONFIG -> Icons.Outlined.Description
            CAMERA_CONFIG -> Icons.Rounded.CameraAlt
        }
    }
}
