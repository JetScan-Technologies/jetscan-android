package io.github.dracula101.jetscan.presentation.features.document.edit.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import timber.log.Timber


@Composable
fun DocumentImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    onZoomImage: (isZoomed: Boolean) -> Unit = {}
) {
    val zoomState = rememberZoomableImageState()
    val zoomed = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        snapshotFlow { zoomState.zoomableState.contentTransformation }
            .collect { contentState ->
                val zoomedScale = contentState.scaleMetadata.userZoom
                if(zoomedScale > 1.0f && !zoomed.value) {
                    onZoomImage(true)
                } else if(zoomedScale <= 1.0f && zoomed.value) {
                    onZoomImage(false)
                }
                zoomed.value = zoomedScale > 1.0f
            }
    }
    ZoomableAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        state = zoomState,
        contentDescription = null,
        modifier = modifier
    )
}

