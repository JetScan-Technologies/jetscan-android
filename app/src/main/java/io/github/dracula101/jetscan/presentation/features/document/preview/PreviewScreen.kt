package io.github.dracula101.jetscan.presentation.features.document.preview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.presentation.features.document.preview.components.PreviewImageGridListItem
import io.github.dracula101.jetscan.presentation.features.document.preview.components.PreviewImageListItem
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    documentId: String?,
    onBack: () -> Unit,
    onNavigateToPdf: (Document) -> Unit,
    onEdit: (Document, Int) -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        if (documentId != null)
            viewModel.trySendAction(PreviewAction.LoadDocument(documentId))
    }
    val hapticFeedback = LocalHapticFeedback.current
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val scannedImages = state.value.scannedDocument?.scannedImages ?: emptyList()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    JetScanScaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            JetScanTopAppbar(
                title = {
                    Text(text = "Preview Screen")
                },
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = onBack
            )
        },
        floatingActionButton = {
            if(state.value.scannedDocument != null){
                ExtendedFloatingActionButton(
                    onClick = {
                        onNavigateToPdf(state.value.scannedDocument!!)
                    },
                    text = { Text("PDF") },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = "Open PDF"
                        )
                    },
                )
            }
        }
    ) {padding, windowSize ->
        when(windowSize) {
            ScaffoldSize.COMPACT -> CompactUiScreen(
                padding = padding,
                scannedImages = scannedImages,
                onReorder = { from, to ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.trySendAction(PreviewAction.Ui.Reorder(from, to))
                },
                onEdit ={ index->
                    if (state.value.scannedDocument != null){
                        onEdit(state.value.scannedDocument!!, index)
                    }
                }
            )
            ScaffoldSize.MEDIUM, ScaffoldSize.EXPANDED -> ExpandedUiScreen(
                padding = padding,
                scannedImages = scannedImages,
                onReorder = { from, to ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.trySendAction(PreviewAction.Ui.Reorder(from, to))
                },
                onEdit ={ index->
                    if (state.value.scannedDocument != null){
                        onEdit(state.value.scannedDocument!!, index)
                    }
                }
            )
        }

    }
}

@Composable
private fun CompactUiScreen(
    padding: PaddingValues,
    scannedImages: List<ScannedImage>,
    onReorder: (from: Int, to: Int) -> Unit = { _, _ -> },
    onEdit: (Int) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val reorderListState = rememberReorderableLazyListState(
        listState = lazyListState,
        onMove = { from, to -> onReorder(from.index, to.index) },
    )
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .reorderable(reorderListState)
            .detectReorderAfterLongPress(reorderListState),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = 48.dp,
        ),
        content = {
            items(scannedImages.size) { index ->
                ReorderableItem(
                    index = index,
                    reorderableState = reorderListState,
                    key = scannedImages[index].scannedUri.toString(),
                    orientationLocked = true,
                    content = { isDragging ->
                        val elevation = animateDpAsState(
                            if (isDragging) 32.dp else 0.dp,
                            label = "Elevation animation"
                        )
                        val scaleAnimation = animateFloatAsState(
                            targetValue = if (isDragging) 0.85f else 1f,
                            label = "Scale animation"
                        )
                        val colorAnimation = animateColorAsState(
                            targetValue = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            label = "Color animation"
                        )
                        PreviewImageListItem(
                            modifier = Modifier
                                .scale(scaleAnimation.value)
                                .graphicsLayer {
                                    shadowElevation = elevation.value.value
                                }
                                .clip(MaterialTheme.shapes.medium)
                                .background(colorAnimation.value),
                            onClick = { onEdit(index) },
                            scannedImage = scannedImages[index],
                            index = index
                        )

                    }
                )
                if (index != scannedImages.lastIndex)
                    HorizontalDivider()
            }
        },
    )
}

@Composable
private fun ExpandedUiScreen(
    padding: PaddingValues,
    scannedImages: List<ScannedImage>,
    onReorder: (from: Int, to: Int) -> Unit,
    onEdit: (Int) -> Unit
) {
    val lazyGridState = rememberLazyGridState()
    val reorderGridState = rememberReorderableLazyGridState(
        gridState = lazyGridState,
        onMove = { from, to -> onReorder(from.index, to.index) },
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(12.dp),
            columns = GridCells.Adaptive(130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(
                bottom = 48.dp,
            ),
        ) {
            items(scannedImages.size) { index ->
                ReorderableItem(
                    index = index,
                    reorderableState = reorderGridState,
                    key = scannedImages[index].scannedUri.toString(),
                    content = { isDragging ->
                        val elevation = animateDpAsState(
                            if (isDragging) 32.dp else 0.dp,
                            label = "Elevation animation"
                        )
                        val scaleAnimation = animateFloatAsState(
                            targetValue = if (isDragging) 0.85f else 1f,
                            label = "Scale animation"
                        )
                        val colorAnimation = animateColorAsState(
                            targetValue = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh,
                            label = "Color animation"
                        )
                        PreviewImageGridListItem(
                            modifier = Modifier
                                .scale(scaleAnimation.value)
                                .graphicsLayer {
                                    shadowElevation = elevation.value.value
                                }
                                .clip(MaterialTheme.shapes.medium)
                                .background(colorAnimation.value),
                            onClick = { onEdit(index) },
                            scannedImage = scannedImages[index],
                            index = index
                        )
                    }
                )
            }
        }
    }
}

