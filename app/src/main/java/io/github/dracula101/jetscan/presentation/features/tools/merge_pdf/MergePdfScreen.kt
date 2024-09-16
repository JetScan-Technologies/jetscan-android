package io.github.dracula101.jetscan.presentation.features.tools.merge_pdf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePdfScreen(
    viewModel: MergePdfViewModel = hiltViewModel(),
    documentId: String?,
    onNavigateBack: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
            FilledTonalButton(
                onClick = {
                    //viewModel.trySendAction(MergePdfAction.Ui.OnMergeClicked)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = state.value.selectedDocuments.size >= 2,
            ) {
                Text(
                    "Merge",
                )
            }
        }
    ) { padding, windowSize ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            Text(
                "2 selected files to be merged",
                style = MaterialTheme.typography.bodyMedium,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            AppTextField(
                value = state.value.fileName,
                onValueChange = {
                    viewModel.trySendAction(MergePdfAction.Ui.OnFileNameChanged(it))
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
            if(state.value.selectedDocuments.firstOrNull() == null){
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
                    document = state.value.selectedDocuments.first(),
                    deleteDocument = {
                        viewModel.trySendAction(MergePdfAction.Ui.OnDocumentDeleted(it))
                    }
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            if (state.value.selectedDocuments.getOrNull(1) != null) {
                DocumentTile(
                    document = state.value.selectedDocuments[1],
                    deleteDocument = {
                        viewModel.trySendAction(MergePdfAction.Ui.OnDocumentDeleted(it))
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
            if (state.value.selectedDocuments.size > 2) {
                state.value.selectedDocuments.subList(2, state.value.selectedDocuments.size).forEach { document ->
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    DocumentTile(
                        document = document,
                        deleteDocument = {
                            viewModel.trySendAction(MergePdfAction.Ui.OnDocumentDeleted(it))
                        }
                    )
                }
            }
            if(state.value.selectedDocuments.size >= 2){
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
    }
}