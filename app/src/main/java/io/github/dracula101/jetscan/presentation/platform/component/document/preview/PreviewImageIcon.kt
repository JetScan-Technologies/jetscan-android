package io.github.dracula101.jetscan.presentation.platform.component.document.preview


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.dracula101.jetscan.R
import timber.log.Timber


@Composable
fun PreviewIcon(modifier: Modifier = Modifier, uri: Uri) {
    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .error(R.drawable.image_error)
            .crossfade(true)
            .data(uri)
            .build(),
        onState = { state ->
            when (state) {
                is AsyncImagePainter.State.Error -> {
                    Timber.e(state.result.throwable)
                }
                else -> {}
            }
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Low,
        modifier = modifier
            .background(Color.White)
    )
}