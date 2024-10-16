package io.github.dracula101.jetscan.presentation.features.home.home_view.components

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeBottomBar
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeViewModel
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItem
import io.github.dracula101.jetscan.presentation.features.home.main.components.DocumentItemUI
import io.github.dracula101.jetscan.presentation.features.home.main.components.EmptyDocumentView
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomePageComponent


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandedHomeScreen(
    isExpanded: Boolean,
    padding: PaddingValues,
    permissionLauncher: ActivityResultLauncher<String>,
    viewModel: MainHomeViewModel,
    state: MainHomeState,
    onDocumentClick: (Document) -> Unit,
    onDocumentDetailClick: (Document) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Row(
        modifier = Modifier
            .padding(padding)
    ) {
        MainHomePageComponent(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .weight(1f),
            maxItemsInEachRow = 2,
            isExpanded = true,
            viewModel = viewModel,
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = lazyListState,
        ) {
            item(key = "header") {
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
                        ui = DocumentItemUI.Compact(
                            onDetailClicked = { onDocumentDetailClick(document) }
                        ),
                    )
                }
            }else {
                item {
                    EmptyDocumentView()
                }
            }
        }
    }
}
