package io.github.dracula101.jetscan.presentation.features.document.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime
import io.github.dracula101.jetscan.presentation.features.document.folder.components.*
import io.github.dracula101.jetscan.presentation.features.home.files_view.AddFolderDialog
import io.github.dracula101.jetscan.presentation.features.home.files_view.components.FolderItem
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDocumentScreen(
    documentId: String,
    path: String,
    onBack: () -> Unit,
    viewModel: FolderDocViewModel = hiltViewModel(),
    onNavigateToDocument: (Document) -> Unit,
    onNavigateToFolder: (DocumentFolder) -> Unit,
    onNavigateToFolderPath: (folderName: String, path: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    LaunchedEffect(Unit) {
        viewModel.trySendAction(FolderDocAction.Internal.LoadFolder(documentId, path))
    }
    state.value.showDocumentFiles.let { showingBottomSheet ->
        if (showingBottomSheet) {
            DocumentFilesBottomSheet(
                onDismiss = {
                    viewModel.trySendAction(FolderDocAction.Ui.HideDocuments)
                },
                documents = state.value.remDocuments,
                onDocumentAdd = {
                    viewModel.trySendAction(FolderDocAction.Ui.AddDocument(it))
                }
            )
        }
    }
    state.value.dialogState?.let {
        FolderDocDialog(
            state = state.value,
            onDismiss = {
                viewModel.trySendAction(FolderDocAction.Alerts.DismissDialog)
            },
            onAddFolder = {folder->
                viewModel.trySendAction(FolderDocAction.Ui.AddFolder(folder))
            }
        )
    }
    JetScanScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            FolderDocTopbar(
                onBack = onBack,
                scrollBehavior = scrollBehavior,
                folder = state.value.folder,
                onMenuOptionClick = { menuOption->
                    when(menuOption){
                        MenuOption.IMPORT_PDF -> {

                        }
                        MenuOption.SELECT -> {
                            if (state.value.folder?.documents?.isNotEmpty() == true) {
                                viewModel.trySendAction(FolderDocAction.Ui.HandleDocumentSelection(state.value.folder?.documents?.first()!!.id))
                            }
                        }
                        MenuOption.CREATE_FOLDER -> {
                            viewModel.trySendAction(FolderDocAction.Alerts.ShowAddFolderDialog(state.value.folder?.name ?: ""))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = "Add Document"
                    )
                },
                text = {
                    Text(text = "Add")
                },
                onClick = {
                    viewModel.trySendAction(FolderDocAction.Ui.ShowDocuments)
                },
            )
        }
    ) { padding, scaffoldSize ->
        if (state.value.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Loading...")
                }
            }
            return@JetScanScaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                FolderLocation(
                    path = "$path/${state.value.folder?.name ?: "Folder"}",
                    onNavigateToFolderPath = onNavigateToFolderPath
                )
            }
            if(state.value.folders?.isNotEmpty() == true){
                itemsIndexed(state.value.folders ?: emptyList()){ index, folder->
                    InternalFolderItem(
                        folder = folder,
                        onClickFolder = {
                            onNavigateToFolder(folder)
                        },
                        onFolderDelete = {},
                    )
                }
            }
            if ((state.value.folder?.documents?.size ?: 0) > 0) {
                itemsIndexed(state.value.folder?.documents ?: emptyList()) { index, document ->
                    FolderDocItem(
                        document = document,
                        onDocumentClick = {
                            val isOneDocumentSelected = state.value.documentInfo?.any { it.second.isSelected } ?: false
                            if (isOneDocumentSelected) {
                                viewModel.trySendAction(FolderDocAction.Ui.HandleDocumentSelection(document.id))
                            } else {
                                onNavigateToDocument(document)
                            }
                        },
                        onRemoveDocument = {
                            viewModel.trySendAction(FolderDocAction.Ui.RemoveDocument(document))
                        },
                        onDocumentSelect = {
                            viewModel.trySendAction(FolderDocAction.Ui.HandleDocumentSelection(document.id))
                        },
                        isSelected = state.value.documentInfo?.get(index)?.second?.isSelected ?: false
                    )
                }

            } else {
                item {
                    NoDocumentView(
                        topPadding = (120.dp - (30 * (state.value.folders?.size ?:0)).dp).coerceAtLeast(20.dp),
                        onDocumentAdd = {
                            viewModel.trySendAction(FolderDocAction.Ui.ShowDocuments)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderDocDialog(
    state: FolderDocState,
    onDismiss: ()-> Unit,
    onAddFolder:(String)-> Unit
){
    when(state.dialogState){
        FolderDocState.FolderDocDialogState.ShowAddDocumentDialog -> {
            AddFolderDialog(
                onDismiss = onDismiss,
                onFolderAdd = { onAddFolder(it) }
            )
        }
        null -> {}
    }
}


@Composable
fun InternalFolderItem(
    folder: DocumentFolder,
    onClickFolder: () -> Unit,
    onFolderDelete: () -> Unit,
){
    val isMenuOpen = remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClickFolder)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = "Folder",
                modifier = Modifier
                    .size(75.dp),
                tint  = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Row {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = "Document Count",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${folder.documents.size} files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = folder.formatDateTime(folder.dateCreated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(
                onClick = {
                    isMenuOpen.value = true
                }
            ){
                AppDropDown(
                    expanded = isMenuOpen.value,
                    onDismissRequest = {
                        isMenuOpen.value = false
                    },
                    offset = DpOffset(20.dp, 0.dp),
                    items = listOf(
                        MenuItem(
                            title = "Delete",
                            icon = Icons.Rounded.FolderDelete,
                            onClick = onFolderDelete
                        )
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More Info",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        )
    }
}