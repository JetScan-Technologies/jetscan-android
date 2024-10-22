package io.github.dracula101.jetscan.presentation.features.import_pdf

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.pdf.ui.PdfLoader
import io.github.dracula101.pdf.ui.PdfReader
import io.github.dracula101.pdf.ui.rememberPdfTransformState
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPdfScreen(
    onNavigateBack: () -> Unit,
    pdfUri: Uri?,
    pdfName: String?,
    viewModel: ImportPdfViewModel = hiltViewModel()
) {
    val pdfLazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(pdfUri){
        if(pdfUri != null){
            viewModel.trySendAction(ImportAction.Internal.LoadPdfUri(pdfUri))
        }
    }

    state.value.importDialogState.let {
        ImportPdfDialog(
            onAction = { action -> viewModel.trySendAction(action) },
            state = it
        )
    }


    JetScanScaffold(
        topBar = {
            JetScanTopAppbar(
                title = {
                    Text(
                        text = pdfName ?: "Import Pdf",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onNavigationIconClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            if(state.value.tempFile != null && pdfUri != null){
                ImportPdfBottomBar(
                    state = state.value,
                    onAction = { action -> viewModel.trySendAction(action) },
                    tempFileBytes = state.value.tempFileBytes,
                    pdfUri = pdfUri,
                    pdfName = pdfName,
                    pdfHasError = state.value.hasPdfError
                )
            }
        }
    ) { padding, _ ->

        if(state.value.tempFile != null){
            val pdfTransformState = rememberPdfTransformState(
                lazyListState = pdfLazyListState,
                file = state.value.tempFile
            )
            PdfReader(
                file = state.value.tempFile,
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(padding),
                lazyListState = pdfLazyListState,
                pdfTransformState = pdfTransformState,
                showMarker = false,
                errorContent = { state ->
                    viewModel.trySendAction(ImportAction.Internal.SetPdfError)
                    when(state.pdfErrorCode){
                        PdfLoader.PdfErrorCode.PASSWORD_PROTECTED -> PasswordProtectFileView {
                            viewModel.trySendAction(ImportAction.Alert.PdfPasswordRequest)
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .padding(padding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.throwable.localizedMessage ?: "An error occurred",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ImportPdfBottomBar(
    state: ImportState,
    onAction: (ImportAction) -> Unit,
    tempFileBytes: Long?,
    pdfUri: Uri,
    pdfName: String?,
    pdfHasError: Boolean,
) {
    val bottomBarScrollState = rememberScrollState()
    val fileActionManager = LocalFileActionManager.current
    val context = LocalContext.current
    val saveFileLauncher = fileActionManager.saveFileWithLauncher { pdfUri }
    Box(
        modifier = Modifier
            .alpha(if (!pdfHasError) 1f else 0.75f)
            .navigationBarsPadding()
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(bottomBarScrollState),
        ) {
            if(tempFileBytes != null){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        tempFileBytes.bytesToSizeAndUnit().first.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        tempFileBytes.bytesToSizeAndUnit().second.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            VerticalDivider(
                modifier = Modifier.fillMaxSize()
            )
            ImportActionTile(
                image = Icons.Rounded.Download,
                title = "Import",
                clickEnabled = !state.isImporting && !pdfHasError,
                onClick = {
                    onAction(ImportAction.Ui.ImportDocument)
                }
            )
            VerticalDivider(
                modifier = Modifier.fillMaxSize()
            )
            ImportActionTile(
                image = Icons.Rounded.IosShare,
                title = "Share",
                clickEnabled = !pdfHasError,
                onClick = {
                    fileActionManager.shareFile(
                        uri = pdfUri,
                        title = pdfName ?: "Imported Pdf",
                        subject = "Sharing pdf: ${pdfName ?: "Imported Pdf"}",
                        onActivityNotFound = {}
                    )
                }
            )
            VerticalDivider(
                modifier = Modifier.fillMaxSize()
            )
            ImportActionTile(
                image = Icons.AutoMirrored.Rounded.OpenInNew,
                title = "Open in",
                clickEnabled = !pdfHasError,
                onClick = {
                    fileActionManager.openFileInOtherApp(
                        uri = pdfUri,
                        onActivityNotFound = {}
                    )
                }
            )
            VerticalDivider(
                modifier = Modifier.fillMaxSize()
            )
            ImportActionTile(
                image = Icons.Rounded.Save,
                title = "Save",
                clickEnabled = !pdfHasError,
                onClick = {
                    val savingFileIntent = fileActionManager.saveFileIntent(
                        uri = pdfUri,
                        title = pdfName ?: "Imported Pdf",
                    )
                    saveFileLauncher.launch(savingFileIntent)
                }
            )
            VerticalDivider(
                modifier = Modifier.fillMaxSize()
            )
            ImportActionTile(
                image = Icons.Rounded.Print,
                title = "Print",
                clickEnabled = !pdfHasError,
                onClick = {
                    fileActionManager.shareToPrinter(
                        uri = pdfUri,
                        subject = "Printing pdf: ${pdfName ?: "Imported Pdf"}",
                        activityNotFound = {}
                    )
                }
            )
        }
        if (state.isImporting) {
            when (val currentState = state.importDocumentState) {
                is ImportDocumentState.InProgress -> {
                    LinearProgressIndicator(
                        progress = {
                            currentState.currentProgress / currentState.totalProgress
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    )
                }

                else -> {}
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}

@Composable
fun ImportActionTile(
    image: ImageVector,
    title: String,
    onClick: () -> Unit,
    clickEnabled: Boolean = true
){
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick, enabled = clickEnabled)
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun PasswordProtectFileView(
    onAskForPassword: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.protect_pdf),
            contentDescription = "Password Protected",
            modifier = Modifier
                .fillMaxSize(0.25f),
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Password protected",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Please enter the password to view the pdf",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedButton(
            onClick = onAskForPassword
        ) {
            Text(text = "Enter Password")
        }
    }

}

@Composable
fun ImportPdfDialog(
    state: ImportState.ImportDialogState?,
    onAction: (ImportAction) -> Unit
){
    when(state){
        ImportState.ImportDialogState.PasswordRequest -> {
            PdfPasswordDialog(
                onAction = onAction
            )
        }
        else -> {}
    }
}

@Composable
fun PdfPasswordDialog(
    onAction: (ImportAction) -> Unit
){
    val password = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AppBasicDialog(
        title = "Unlock Pdf",
        titlePadding = PaddingValues(vertical = 12.dp),
    ) {
        Text(
            text = "Enter the password to view the pdf",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = "Password",
            modifier = Modifier.focusRequester(focusRequester),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onAction(
                        ImportAction.Ui.UnlockPdf(password = password.value)
                    )
                }
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        ElevatedButton(
            onClick = {
                focusManager.clearFocus()
                onAction(
                    ImportAction.Ui.UnlockPdf(password = password.value)
                )
            },
            modifier = Modifier
                .align(Alignment.End)
        ) {
            Text(text = "Submit")
        }
    }
}