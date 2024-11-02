package io.github.dracula101.jetscan.presentation.features.tools.split_pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.presentation.features.tools.split_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.button.GradientButton
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import kotlinx.coroutines.launch

@Composable
fun SplitPdfScreen(
    onNavigateBack: () -> Unit,
    documentId: String?,
    viewModel: SplitPdfViewModel = hiltViewModel()
){
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Split PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        },
        bottomBar = {
            if(state.value.view == SplitPdfView.DOCUMENT){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ){
                    GradientButton(
                        text = "Split",
                        onClick = {
                            viewModel.trySendAction(SplitPdfAction.Ui.OnSplitClicked)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.value.selectedDocument != null && !state.value.hasRangeError,
                        showContent = state.value.isLoading,
                    )
                }
            }
        }
    ) { padding, windowSize->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ){
            when(state.value.view){
                SplitPdfView.DOCUMENT -> {
                    SplitPdfDocumentView(
                        documentId = documentId,
                        state = state.value,
                        onAction = {
                            viewModel.trySendAction(it)
                        }
                    )
                }
                SplitPdfView.COMPLETED -> {
                    SplitPdfCompletedView(
                        state = state.value,
                        onAction = {
                            viewModel.trySendAction(it)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPdfDocumentView(
    documentId: String?,
    state: SplitPdfState,
    onAction: (SplitPdfAction) -> Unit
){
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    LaunchedEffect(documentId) {
        documentId?.let { id ->
            onAction(SplitPdfAction.Internal.LoadDocument(id))
        }
    }

    if(bottomSheetState.isVisible){
        DocumentFilesBottomSheet(
            onDismiss = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            },
            documents = state.documents,
            documentClick = { document ->
                onAction(SplitPdfAction.Ui.OnDocumentSelected(document))
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        )
    }

    Text(
        "Select a PDF file to split. The file will be split into multiple PDF files based on the number of pages you specify.",
        style = MaterialTheme.typography.bodyMedium,
    )
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Spacer(modifier = Modifier.padding(vertical = 8.dp))
    AppTextField(
        value = state.fileName,
        onValueChange = {
            onAction(SplitPdfAction.Ui.OnFileNameChanged(it))
        },
        label = "File name",
        modifier = Modifier
            .fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
    )
    Spacer(modifier = Modifier.padding(vertical = 8.dp))
    if(state.selectedDocument == null){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
                .clip(MaterialTheme.shapes.large)
                .customContainer(MaterialTheme.shapes.large)
                .height(80.dp),
            contentAlignment = Alignment.Center
        ){
            Row {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add",
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "Select the file",
                )
            }
        }
    }else {
        DocumentTile(
            document = state.selectedDocument,
            deleteDocument = {
                onAction(SplitPdfAction.Ui.OnDocumentDeleted)
            }
        )
    }
    if(state.selectedDocument != null){
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            "Ranges",
            style = MaterialTheme.typography.titleLarge,
        )
        AppTextField(
            value = state.rangesString,
            onValueChange = {
                onAction(SplitPdfAction.Ui.OnRangeChanged(it))
            },
            label = "Enter Ranges (e.g. 1,2-3,5)",
            modifier = Modifier
                .fillMaxWidth(),
            isError = state.hasRangeError,
            errorText = "Invalid range format entered",
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
        )
        if (state.splitRanges.isNotEmpty()){
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                state.splitRanges.forEach { range ->
                    Text(
                        "Page $range",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(70.dp)
                    ){
                        AsyncImage(
                            model = state.selectedDocument.scannedImages.getOrNull(range.start-1)?.scannedUri,
                            contentDescription = "Page ${range.start}",
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .aspectRatio(3 / 4f)
                                .background(MaterialTheme.colorScheme.surface)
                        )
                        if (range.end != null){
                            repeat(range.end - range.start){
                                Spacer(modifier = Modifier.width(8.dp))
                                AsyncImage(
                                    model = state.selectedDocument.scannedImages.getOrNull(range.start + it)?.scannedUri,
                                    contentDescription = "Page ${it + 1}",
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .aspectRatio(3 / 4f)
                                        .background(MaterialTheme.colorScheme.surface)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SplitPdfCompletedView(
    state: SplitPdfState,
    onAction: (SplitPdfAction) -> Unit
){
    val fileActionManager = LocalFileActionManager.current
    val context = LocalContext.current
    Text(
        "Pdf split successfully, you can find share the files from the document list",
        style = MaterialTheme.typography.bodyMedium,
    )
    HorizontalDivider(
        modifier = Modifier.padding(top = 8.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    state.splitRanges.forEachIndexed { index, range ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .customContainer(MaterialTheme.shapes.medium),
        ){
            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = state.selectedDocument?.scannedImages?.getOrNull(range.start-1)?.scannedUri,
                    contentDescription = "Page ${range.start}",
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .height(70.dp)
                        .aspectRatio(3 / 4f)
                        .background(MaterialTheme.colorScheme.surface)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ){
                    Text(
                        state.outputFiles[index].name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Page $range",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fileUri = remember {
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        state.outputFiles[index]
                    )
                }
                val fileSaveLauncher = fileActionManager.saveFileWithLauncher {fileUri}
                Row(
                    modifier = Modifier
                        .clickable {
                            fileSaveLauncher.launch(fileActionManager.saveFileIntent(fileUri, state.outputFiles[index].name))
                        }
                        .padding(12.dp)
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Rounded.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                VerticalDivider(
                    modifier = Modifier
                        .height(20.dp)
                )
                Row(
                    modifier = Modifier
                        .clickable {
                            fileActionManager.shareFile(
                                fileUri,
                                state.outputFiles[index].name,
                                "Share PDF",
                                onActivityNotFound = {}
                            )
                        }
                        .padding(8.dp)
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Rounded.IosShare,
                        contentDescription = "Share",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


