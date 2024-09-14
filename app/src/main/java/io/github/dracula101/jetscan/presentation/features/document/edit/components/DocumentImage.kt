package io.github.dracula101.jetscan.presentation.features.document.edit.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState


@Composable
fun DocumentImage(
    uri: Uri,
    modifier: Modifier = Modifier,
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
        modifier = modifier
    )
}

