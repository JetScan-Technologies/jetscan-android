package io.github.dracula101.jetscan.presentation.features.home.home_view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.errorprone.annotations.Modifier
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.document.pdfview.PdfViewAction
import io.github.dracula101.jetscan.presentation.features.home.home_view.components.CompactHomeScreen
import io.github.dracula101.jetscan.presentation.features.home.home_view.components.ExpandedHomeScreen
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeViewModel
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.AppBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentAction
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentDetailBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainHomeViewModel,
    windowSize: ScaffoldSize,
    padding: PaddingValues,
    onDocumentClick: (Document) -> Unit,
    onNavigateToPdfActions: (Document, MainHomeSubPage) -> Unit,
    allowImageForImport: Boolean,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            Timber.d("File picked: $uri")
            viewModel.trySendAction(MainHomeAction.Alerts.ImportQualityAlert(uri))
        } else {
            viewModel.trySendAction(MainHomeAction.Alerts.FileNotSelectedAlert)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Timber.d("Permission granted: $isGranted")
            if (isGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2)
                filePickerLauncher.launch(
                    if (allowImageForImport) arrayOf("application/pdf", "image/*")
                    else arrayOf("application/pdf")
                )
        }
    )
    val documentDetailItem = remember { mutableStateOf<Document?>(null) }
    val fileActionManager = LocalFileActionManager.current
    val saveToStorageLauncher = fileActionManager.saveFileWithLauncher { documentDetailItem.value!!.uri }

    if(bottomSheetState.isVisible) {
        if (documentDetailItem.value != null) {
            DocumentDetailBottomSheet(
                document = documentDetailItem.value!!,
                onDismiss = {
                    scope.launch { bottomSheetState.hide() }
                },
                onAction = {action->
                    scope.launch {
                        bottomSheetState.hide()
                        val fileUri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            documentDetailItem.value!!.uri.toFile()
                        )
                        when(action){
                            DocumentAction.SAVE_TO_DEVICE -> {
                                val intent = fileActionManager.saveFileIntent(
                                    fileUri,
                                    documentDetailItem.value!!.name
                                )
                                saveToStorageLauncher.launch(intent)
                            }
                            DocumentAction.SHARE -> {
                                fileActionManager.shareFile(
                                    uri = fileUri,
                                    title = documentDetailItem.value!!.name,
                                    subject = "Share PDF",
                                    onActivityNotFound = {}
                                )
                            }
                            DocumentAction.RENAME -> {
                            }
                            DocumentAction.PRINT -> {
                                fileActionManager.shareToPrinter(
                                    uri = fileUri,
                                    subject = "Print ${documentDetailItem.value!!.name}",
                                    activityNotFound = {}
                                )
                            }
                            DocumentAction.DELETE -> {
                                viewModel.trySendAction(MainHomeAction.Alerts.DeleteDocumentAlert(documentDetailItem.value!!))
                            }

                            DocumentAction.WATERMARK, DocumentAction.DIGITAL_SIGNATURE, DocumentAction.SPLIT, DocumentAction.MERGE, DocumentAction.PROTECT, DocumentAction.COMPRESS -> {
                                onNavigateToPdfActions(
                                    documentDetailItem.value!!,
                                    when(action){
                                        DocumentAction.WATERMARK -> MainHomeSubPage.WATERMARK
                                        DocumentAction.DIGITAL_SIGNATURE -> MainHomeSubPage.ESIGN_PDF
                                        DocumentAction.SPLIT -> MainHomeSubPage.SPLIT_PDF
                                        DocumentAction.MERGE -> MainHomeSubPage.MERGE_PDF
                                        DocumentAction.PROTECT -> MainHomeSubPage.PROTECT_PDF
                                        DocumentAction.COMPRESS -> MainHomeSubPage.COMPRESS_PDF
                                        else -> MainHomeSubPage.WATERMARK
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    when (windowSize) {
        ScaffoldSize.COMPACT -> {
            CompactHomeScreen(
                padding = padding,
                permissionLauncher = permissionLauncher,
                viewModel = viewModel,
                state = state.value,
                onDocumentClick = onDocumentClick,
                onDocumentDetailClick = {
                    documentDetailItem.value = it
                    scope.launch {
                        bottomSheetState.show()
                    }
                },
            )
        }

        else -> {
            ExpandedHomeScreen(
                isExpanded = windowSize == ScaffoldSize.EXPANDED,
                padding = padding,
                permissionLauncher = permissionLauncher,
                viewModel = viewModel,
                state = state.value,
                onDocumentClick = onDocumentClick,
                onDocumentDetailClick = {
                    documentDetailItem.value = it
                    scope.launch {
                        bottomSheetState.show()
                    }
                },
            )
        }
    }

}