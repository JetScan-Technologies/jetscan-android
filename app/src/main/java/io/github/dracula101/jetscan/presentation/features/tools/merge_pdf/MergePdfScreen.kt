package io.github.dracula101.jetscan.presentation.features.tools.merge_pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.extensions.formatDate
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.document.preview.PreviewIcon
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MergePdfScreen(
    viewModel: MergePdfViewModel = hiltViewModel(),
    documentId: String?,
    onNavigateBack: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val fileActionManager = LocalFileActionManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (documentId != null) {
            viewModel.trySendAction(
                MergePdfAction.Internal.LoadDocument(documentId)
            )
        }
    }

    if(bottomSheetState.isVisible){
        DocumentFilesBottomSheet(
            onDismiss = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            },
            documents = state.value.documents,
            documentClick = { document ->
                viewModel.trySendAction(MergePdfAction.Ui.OnDocumentSelected(document))
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        )
    }

    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Merge PDF",
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                )
            }
        },
        bottomBar = {
            when(state.value.view){
                MergePdfView.DOCUMENT -> FilledTonalButton(
                    onClick = {
                        viewModel.trySendAction(MergePdfAction.Ui.OnMergeDocument)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = state.value.selectedDocuments.size >= 2,
                ) { Text("Merge") }
                MergePdfView.MERGED -> FilledTonalButton(
                    onClick = {
                        if (state.value.mergedDocument != null){
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                context.applicationContext.packageName + ".provider",
                                state.value.mergedDocument!!
                            )
                            fileActionManager.shareFile(
                                fileUri,
                                "Sharing merged PDF",
                                "${state.value.fileName}.pdf",
                                onActivityNotFound = {}
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = state.value.selectedDocuments.size >= 2,
                ) { Text("Share PDF") }
            }
        }
    ) { padding, windowSize ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ){
            when(state.value.view){
                MergePdfView.DOCUMENT -> MergeDocumentScreen(
                    state = state.value,
                    onAction = { action ->
                        viewModel.trySendAction(action)
                    },
                    coroutineScope = coroutineScope,
                    bottomSheetState = bottomSheetState,
                )
                MergePdfView.MERGED -> {
                    Text(
                        "PDF File is Merged",
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "File Name\n${state.value.fileName}\n${state.value.mergedDocument?.length()?.bytesToReadableSize()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .customContainer(
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(vertical = 8.dp),
                    ) {
                        state.value.selectedDocuments.forEachIndexed { index, document ->
                            document.previewImageUri?.let {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PreviewIcon(
                                        uri = it,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .weight(0.7f)
                                    )
                                    Column(
                                        modifier = Modifier.weight(3f)
                                    ) {
                                        Text(
                                            document.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Visible
                                        )
                                        Spacer(modifier = Modifier.padding(4.dp))
                                        Text(
                                            "${document.size.bytesToSizeAndUnit().first} ${document.size.bytesToSizeAndUnit().second} - ${document.formatDate(document.dateCreated)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                                if(index != state.value.selectedDocuments.size - 1){
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally),
                                        contentAlignment = Alignment.Center
                                    ){
                                        VerticalDivider(
                                            modifier = Modifier
                                                .height(50.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            thickness = 5.dp
                                        )
                                        Box (
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ){
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Add",
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeDocumentScreen(
    state: MergePdfState,
    onAction: (MergePdfAction) -> Unit,
    coroutineScope: CoroutineScope,
    bottomSheetState: SheetState,
){
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Text(
        "2 selected files to be merged",
        style = MaterialTheme.typography.bodyMedium,
    )
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Spacer(modifier = Modifier.padding(vertical = 8.dp))
    AppTextField(
        value = state.fileName,
        onValueChange = {
            onAction(MergePdfAction.Ui.OnFileNameChanged(it))
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
    if(state.selectedDocuments.firstOrNull() == null){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
                .clip(MaterialTheme.shapes.medium)
                .customContainer(MaterialTheme.shapes.medium)
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
                    "Select the 1st file",
                )
            }
        }
    }else {
        DocumentTile(
            document = state.selectedDocuments.first(),
            deleteDocument = {
                onAction(MergePdfAction.Ui.OnDocumentDeleted(it))
            }
        )
    }
    Spacer(modifier = Modifier.padding(vertical = 8.dp))
    if (state.selectedDocuments.getOrNull(1) != null) {
        DocumentTile(
            document = state.selectedDocuments[1],
            deleteDocument = {
                onAction(MergePdfAction.Ui.OnDocumentDeleted(it))
            }
        )
    }else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
                .clip(MaterialTheme.shapes.medium)
                .customContainer(MaterialTheme.shapes.medium)
                .height(80.dp),
            contentAlignment = Alignment.Center
        ){
            Row {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Back",
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "Select the 2nd file",
                )
            }
        }
    }
    // show more files from 2 onwards
    if (state.selectedDocuments.size > 2) {
        state.selectedDocuments.subList(2, state.selectedDocuments.size).forEach { document ->
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            DocumentTile(
                document = document,
                deleteDocument = {
                    onAction(MergePdfAction.Ui.OnDocumentDeleted(it))
                }
            )
        }
    }
    if(state.selectedDocuments.size >= 2){
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        ElevatedButton(
            onClick = {
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Merge",
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                "Add More Files",
            )
        }
    }
    Spacer(modifier = Modifier.height(200.dp))
}