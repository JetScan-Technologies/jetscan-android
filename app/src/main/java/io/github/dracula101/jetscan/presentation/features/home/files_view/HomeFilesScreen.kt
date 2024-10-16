package io.github.dracula101.jetscan.presentation.features.home.files_view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.features.home.files_view.components.FileTopbar
import io.github.dracula101.jetscan.presentation.features.home.files_view.components.FolderItem
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItem
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItemUI
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentAction
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentDetailBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.dialog.IconAlertDialog
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeFilesScreen(
    viewModel: HomeFilesViewModel,
    windowSize: ScaffoldSize,
    padding: PaddingValues,
    mainHomeState: MainHomeState,
    onShowSnackbar: (SnackbarState) -> Unit,
    onDocumentClick: (Document) -> Unit,
    onNavigateToFolder: (DocumentFolder) -> Unit,
    onNavigateToPdfActions: (Document?, MainHomeSubPage) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val fileActionManager = LocalFileActionManager.current
    val documentDetailItem = remember { mutableStateOf<Document?>(null) }
    val saveToStorageLauncher = fileActionManager.saveFileWithLauncher { documentDetailItem.value!!.uri }
    val bottomSheetState = rememberModalBottomSheetState()
    val layoutDirection = LocalLayoutDirection.current

    state.value.dialogState?.let { dialogState->
        HomeFilesDialog(
            dialogState,
            onFolderAdd = { name ->
                viewModel.trySendAction(HomeFilesAction.Ui.AddFolder(name))
            },
            onFolderDelete = { folder ->
                viewModel.trySendAction(HomeFilesAction.Ui.DeleteFolder(folder))
            },
            onDismiss = {
                viewModel.trySendAction(HomeFilesAction.Ui.DismissDialog)
            }
        )
    }
    state.value.snackbarState?.let { snackbarState ->
        onShowSnackbar(snackbarState)
        viewModel.trySendAction(HomeFilesAction.Ui.DismissSnackbar)
    }
    if(bottomSheetState.isVisible){
        if (documentDetailItem.value != null ){
            DocumentDetailBottomSheet(
                document = documentDetailItem.value!!,
                onDismiss = {
                    scope.launch { bottomSheetState.hide() }
                },
                onAction = { action ->
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
                                    file = fileUri.toFile(),
                                    subject = "Print ${documentDetailItem.value!!.name}",
                                    activityNotFound = {}
                                )
                            }
                            DocumentAction.DELETE -> {
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
    state.value.pdfActionPage?.let { pdfActionPage ->
        onNavigateToPdfActions(pdfActionPage.document, pdfActionPage.page)
    }
    LazyVerticalGrid(
        modifier = Modifier
            .padding(
                top = padding.calculateTopPadding(),
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection)
            )
            .padding(horizontal = 16.dp),
        state = gridState,
        columns = GridCells.Fixed(
            when(windowSize){
                ScaffoldSize.COMPACT -> 3
                ScaffoldSize.MEDIUM -> 6
                ScaffoldSize.EXPANDED -> 9
            }
        ),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(
            span = {
                GridItemSpan(
                    when(windowSize){
                        ScaffoldSize.COMPACT -> 3
                        ScaffoldSize.MEDIUM -> 6
                        ScaffoldSize.EXPANDED -> 9
                    }
                )
            }
        ) {
            FileTopbar(
                state = state.value,
                mainHomeState = mainHomeState,
                onFolderShowAlert = {
                    viewModel.trySendAction(HomeFilesAction.Alerts.ShowAddFolderAlert)
                },
            )
        }
        if(state.value.folders.isEmpty() && state.value.documents.isEmpty()){
            item (
                span = {
                    GridItemSpan(
                        when(windowSize){
                            ScaffoldSize.COMPACT -> 3
                            ScaffoldSize.MEDIUM -> 6
                            ScaffoldSize.EXPANDED -> 9
                        }
                    )
                }
            ){
                NoFolderView(
                    modifier = Modifier
                        .padding(top = 32.dp)
                )
            }
        }else {
            itemsIndexed(
                items = state.value.folders.reversed(),
                key = { _, folder -> folder.id },
                itemContent = { _, folder ->
                    FolderItem(
                        folder = folder,
                        modifier = Modifier
                            .animateItemPlacement(),
                        onFolderDelete = {
                            viewModel.trySendAction(
                                HomeFilesAction.Alerts.ShowDeleteFolderAlert(folder)
                            )
                        },
                        onClickFolder = {
                            onNavigateToFolder(folder)
                        },
                    )
                }
            )
            if(state.value.folders.isNotEmpty() && state.value.documents.isNotEmpty()){
                item (
                    span = {
                        GridItemSpan(
                            when(windowSize){
                                ScaffoldSize.COMPACT -> 3
                                ScaffoldSize.MEDIUM -> 6
                                ScaffoldSize.EXPANDED -> 9
                            }
                        )
                    }
                ){
                    Text(
                        text = "Files",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    )
                }
            }
            itemsIndexed(
                items = state.value.documents,
                span = { _, folder ->
                    GridItemSpan(3)
                },
                key = { _, document -> document.id },
                itemContent = { _, document ->
                    DocumentItem(
                        document = document,
                        onClick = {
                            onDocumentClick(document)
                        },
                        modifier = Modifier
                            .animateItemPlacement(),
                        ui = DocumentItemUI.Compact(
                            onDetailClicked = {
                                documentDetailItem.value = document
                                scope.launch {
                                    bottomSheetState.show()
                                }
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun HomeFilesDialog(
    dialogState: HomeFilesDialogState,
    onFolderAdd: (String)->Unit,
    onFolderDelete: (DocumentFolder)->Unit,
    onDismiss: () -> Unit
){
    when(dialogState){
        is HomeFilesDialogState.ShowAddFolderDialog -> AddFolderDialog(
            onFolderAdd = onFolderAdd,
            onDismiss = onDismiss
        )
        is HomeFilesDialogState.ShowDeleteFolderDialog -> DeleteFolderDialog(
            onDismiss = onDismiss,
            onFolderDelete = { onFolderDelete(dialogState.folder) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderDialog(
    onFolderAdd: (String) -> Unit,
    onDismiss: ()->Unit,
) {
    val folderName = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit){
        delay(500)
        focusRequester.requestFocus()
    }
    AppBasicDialog(
        title = "Add Folder",
        content = {
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .focusRequester(focusRequester = focusRequester)
                    .padding(vertical = 8.dp),
                value = folderName.value,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = {
                    Icon(
                        imageVector = if (folderName.value.contains("/"))
                            Icons.Rounded.Cancel else Icons.Rounded.Folder,
                        contentDescription = "Prefix Icons",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onValueChange = { name ->
                    folderName.value = name
                },
                isError = folderName.value.contains("/"),
                label = {
                    Text(
                        text = if(folderName.value.contains("/")) "Folder name cannot contain '/'" else "Folder Name",
                        color = if(folderName.value.contains("/")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                },
                placeholder = {
                    Text(
                        text = "Enter Folder Name",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                    autoCorrect = false
                ),
            )
            Spacer(modifier = Modifier.size(8.dp))
        },
        actions = {
            OutlinedButton(onClick = {onDismiss()}) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            FilledTonalButton(onClick = { onFolderAdd(folderName.value) }, enabled = folderName.value.isNotEmpty().and(!folderName.value.contains("/"))) {
                Text(
                    text = "Add",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun DeleteFolderDialog(
    onFolderDelete: ()->Unit,
    onDismiss: ()->Unit
){
    IconAlertDialog(
        icon = Icons.Rounded.FolderDelete,
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete this folder?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "All files will be removed from this folder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        },
        cancelText = "Cancel",
        confirmText = "Delete",
        onConfirm = onFolderDelete,
        onCancel = onDismiss
    )
}

@Composable
fun NoFolderView(
    modifier : Modifier = Modifier
){
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.no_folder_bg),
                contentDescription = "No Folders",
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
            )
            Text(
                text = "No Folders or Documents",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "You can organize your documents into folders by adding them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
