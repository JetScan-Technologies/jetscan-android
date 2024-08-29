package io.github.dracula101.jetscan.presentation.features.home.home_view.components

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeViewModel
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomePageComponent
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItem
import io.github.dracula101.jetscan.presentation.features.home.main.components.EmptyDocumentView


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactHomeScreen(
    padding: PaddingValues,
    permissionLauncher: ActivityResultLauncher<String>,
    viewModel: MainHomeViewModel,
    state: MainHomeState,
    onDocumentClick: (Document) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .padding(padding),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            MainHomePageComponent(viewModel = viewModel)
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
        stickyHeader {
            DocumentsListTitle {
                if (state.importDocumentState == null) {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    viewModel.trySendAction(MainHomeAction.Alerts.ImportDocumentInProgressAlert)
                }
            }
            state.importDocumentState?.let { importDocumentState ->
                DocumentImportingState(
                    importDocumentState = importDocumentState,
                    onCancel = {
                        viewModel.trySendAction(MainHomeAction.ImportDocumentState.Cancelled)
                    }
                )
            }
        }
        if (state.documents.isNotEmpty()){
            items(
                items = state.documents,
                key = { item -> item.id }
            ) { document ->
                DocumentItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItemPlacement(),
                    document = document,
                    onClick = { onDocumentClick(document) },
                    onDeleteClicked = {
                        viewModel.trySendAction(
                            MainHomeAction.Alerts.DeleteDocumentAlert(document)
                        )
                    }
                )
            }
        }else {
            item{
                EmptyDocumentView(
                    modifier = Modifier
                        .padding(top = 20.dp)
                )
            }
        }
    }
}