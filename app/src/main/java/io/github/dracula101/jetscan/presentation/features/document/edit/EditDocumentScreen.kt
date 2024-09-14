package io.github.dracula101.jetscan.presentation.features.document.edit

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CropOriginal
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Rotate90DegreesCw
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.document.edit.components.DocumentImage
import io.github.dracula101.jetscan.presentation.features.document.edit.components.EditPdfActionTile
import io.github.dracula101.jetscan.presentation.platform.component.appbar.BackButtonIcon
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.boxShadow
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.fadingEdge
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import timber.log.Timber
import kotlin.math.absoluteValue

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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val density = LocalDensity.current
    val horizontalScrollState = rememberScrollState()
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize(),
                pageSpacing = 2.dp,
            ) { page ->
                DocumentImage(
                    state.value.scannedDocument!!.scannedImages[page].scannedUri,
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ){
                Row(
                    modifier = Modifier
                        .padding(bottom = WindowInsets.navigationBars.getBottom(density).dp * 0.5f)
                        .fillMaxWidth()
                        .boxShadow(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            blurRadius = 8.dp,
                            offsetX = 2.dp,
                            offsetY = 2.dp
                        )
                        .padding(horizontal = 12.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fadingEdge(
                            Brush.horizontalGradient(
                                0.95f to MaterialTheme.colorScheme.surface,
                                1f to Color.Transparent,
                            )
                        )
                        .height(bottomBarHeight)
                        .horizontalScroll(horizontalScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    EditPdfActionTile(
                        title = "Crop",
                        icon = Icons.Rounded.CropOriginal,
                        onClick = { },
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    EditPdfActionTile(
                        title = "Rotate",
                        icon = Icons.Rounded.Rotate90DegreesCw,
                        onClick = { },
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    EditPdfActionTile(
                        title = "Filter",
                        icon = Icons.Rounded.Filter,
                        onClick = { },
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
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
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    EditPdfActionTile(
                        title = "Print",
                        icon = Icons.Rounded.Print,
                        onClick = { },
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
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
                    VerticalDivider(
                        modifier = Modifier
                            .height(bottomBarHeight * 0.8f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    EditPdfActionTile(
                        title = "Share",
                        icon = Icons.Rounded.IosShare,
                        onClick = { },
                    )
                }
            }
        }
    }
}

