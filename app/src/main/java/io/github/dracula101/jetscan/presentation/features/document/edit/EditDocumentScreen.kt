package io.github.dracula101.jetscan.presentation.features.document.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CropOriginal
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Rotate90DegreesCw
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.features.document.edit.components.DocumentImage
import io.github.dracula101.jetscan.presentation.features.document.edit.components.EditPdfActionTile
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.text.FittedText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditDocumentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPdf: (Document) -> Unit,
    documentId: String,
    documentPageIndex: Int,
    viewModel: EditDocViewModel = hiltViewModel(),
    bottomBarHeight: Dp = 60.dp
) {
    LaunchedEffect(Unit) {
        viewModel.trySendAction(EditDocAction.LoadDocument(documentId))
    }
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        pageCount = { state.value.scannedDocument?.scannedImages?.size ?: 0 },
        initialPage = documentPageIndex
    )
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val density = LocalDensity.current
    val widthAnimation = remember { Animatable(1f) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    JetScanScaffold(
        topBar = {
            JetScanTopAppbar(
                title = { Text("Edit Document") },
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = onNavigateBack
            )
        },
    ) { padding, windowsize ->
        if (state.value.scannedDocument != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
            ){
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(padding),
                ){
                    VerticalPager(
                        state = pagerState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val image = state.value.scannedDocument!!.scannedImages[it]
                        DocumentImage(
                            uri = image.scannedUri,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    if (pagerState.currentPage > 0){
                        IconButton(
                            onClick = {
                                val currentPage = pagerState.currentPage
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        currentPage - 1,
                                        animationSpec = tween(500)
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = "Go previous",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (pagerState.currentPage < state.value.scannedDocument!!.scannedImages.size - 1){
                        IconButton(
                            onClick = {
                                val currentPage = pagerState.currentPage
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        currentPage + 1,
                                        animationSpec = tween(500)
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDownward,
                                contentDescription = "Go next",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (widthAnimation.value == 0f){
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    widthAnimation.animateTo(1f)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Open sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                }
                if (widthAnimation.value == 1f){
                    Box(
                        modifier = Modifier
                            .offset(x = (150 * (1 - widthAnimation.value)).dp)
                            .width(IntrinsicSize.Min)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .align(Alignment.TopCenter)
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EditPdfActionTile(
                                title = "Crop",
                                icon = Icons.Rounded.CropOriginal,
                                onClick = { },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Rotate",
                                icon = Icons.Rounded.Rotate90DegreesCw,
                                onClick = { },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Filter",
                                icon = Icons.Rounded.Filter,
                                onClick = { },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Open PDF",
                                icon = Icons.Rounded.PictureAsPdf,
                                onClick = {
                                    if (state.value.scannedDocument != null) {
                                        onNavigateToPdf(state.value.scannedDocument!!)
                                    }
                                },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Rename",
                                icon = Icons.Rounded.Edit,
                                onClick = { },
                            )
                            // VerticalDivider(
                            //     modifier = Modifier
                            //         .height(bottomBarHeight * 0.8f),
                            //     color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            // )
                            // EditPdfActionTile(
                            //     title = "Add",
                            //     icon = Icons.Rounded.Rotate90DegreesCw,
                            //     onClick = { },
                            //     modifier = Modifier.width(60.dp).padding(8.dp)
                            // )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Share",
                                icon = Icons.Rounded.IosShare,
                                onClick = { },
                            )
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        ),
                                        endY = 110f,
                                    ),
                                )
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        widthAnimation.animateTo(0f)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = "Close"
                                )
                            }
                            FittedText(
                                text = state.value.scannedDocument!!.scannedImages[pagerState.currentPage].size.bytesToSizeAndUnit().first.toString(),
                                fontWeight = FontWeight.ExtraLight,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                            )
                            Text(
                                state.value.scannedDocument!!.scannedImages[pagerState.currentPage].size.bytesToSizeAndUnit().second,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

