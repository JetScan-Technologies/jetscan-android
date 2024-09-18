package io.github.dracula101.jetscan.presentation.features.document.scanner.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage


@Composable
fun PreviewImage(
    modifier: Modifier,
    aspectRatio: Float,
    document: CameraScannedImage?,
    onImageClick: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .aspectRatio(aspectRatio)
                .heightIn(min = 40.dp, max = 70.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (document?.croppedImage != null) {
                Image(
                    bitmap = (document.filteredImage ?: document.croppedImage).asImageBitmap(),
                    contentDescription = "Document",
                    modifier = Modifier
                        .shadow(
                            16.dp,
                            MaterialTheme.shapes.medium,
                            ambientColor = MaterialTheme.colorScheme.primary
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
