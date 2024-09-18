package io.github.dracula101.jetscan.presentation.features.home.files_view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.features.home.files_view.components.FileTopbar
import io.github.dracula101.jetscan.presentation.features.home.files_view.components.FolderItem
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItem
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.dialog.IconAlertDialog
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFilesScreen(
    viewModel: HomeFilesViewModel,
    windowSize: ScaffoldSize,
    padding: PaddingValues,
    mainHomeState: MainHomeState,
    onShowSnackbar: (SnackbarState) -> Unit,
    onNavigateToFolder: (DocumentFolder) -> Unit
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
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
    LazyColumn(
        modifier = Modifier
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
            .padding(horizontal = 16.dp)
    ) {
        stickyHeader {
            FileTopbar(
                state = state.value,
                mainHomeState = mainHomeState,
                onFolderShowAlert = {
                    viewModel.trySendAction(HomeFilesAction.Alerts.ShowAddFolderAlert)
                },
            )
        }
        if(state.value.folders.isEmpty() && state.value.documents.isEmpty()){
            item {
                NoFolderView(
                    modifier = Modifier
                        .padding(top = 32.dp)
                )
            }
        }
        seperatedItems(
            items = state.value.folders.reversed(),
            key = { it.dateCreated },
            itemContent =  { folder->
                FolderItem(
                    folder = folder,
                    modifier = Modifier
                        .animateItemPlacement(),
                    onFolderDelete = {
                        viewModel.trySendAction(HomeFilesAction.Alerts.ShowDeleteFolderAlert(folder))
                    },
                    onClickFolder = {
                        onNavigateToFolder(folder)
                    },
                )
            },
            separatorContent = {
                Spacer(modifier = Modifier.size(12.dp))
            }
        )
        item{
            Spacer(modifier = Modifier.size(12.dp))
        }
        seperatedItems(
            items = state.value.documents,
            key = { it.id },
            itemContent = { document ->
                DocumentItem(
                    document = document,
                    onClick = {  },
                    modifier = Modifier
                        .animateItemPlacement()
                )
            },
            separatorContent = {
                Spacer(modifier = Modifier.size(12.dp))
            }
        )
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

@Composable
fun AddFolderDialog(
    onFolderAdd: (String) -> Unit,
    onDismiss: ()->Unit,
) {
    val folderName = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
    AppBasicDialog(
        title = "Add Folder",
        content = {
            Column {
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
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
                Spacer(modifier = Modifier.size(16.dp))
            }
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
                modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth()
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

fun <T> LazyListScope.seperatedItems(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    contentType: (item: T) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
    separatorContent: @Composable LazyItemScope.() -> Unit
) = items(items.size) { index ->
    val item = items[index]
    itemContent(item)
    if (index < items.size - 1) {
        separatorContent()
    }
}
