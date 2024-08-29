package io.github.dracula101.jetscan.presentation.features.document.edit

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.dracula101.jetscan.presentation.platform.component.appbar.BackButtonIcon
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditDocumentScreen(
    onNavigateBack: () -> Unit,
    documentId: String,
    documentPageIndex: Int,
    viewModel: EditDocViewModel = hiltViewModel()
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

    JetScanScaffold(
        topBar = {
            JetScanTopAppbar(
                title = { Text("Edit Document") },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButtonIcon() },
                onNavigationIconClick = onNavigateBack
            )
        },
    ) { padding, windowsize ->
        if (state.value.scannedDocument != null) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                pageSpacing = 2.dp,
            ) { page ->
                DocumentImage(
                    state.value.scannedDocument!!.scannedImages[page].scannedUri,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}


@Composable
private fun DocumentImage(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val zoomState = rememberZoomableImageState()
    ZoomableAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        state = zoomState,
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

