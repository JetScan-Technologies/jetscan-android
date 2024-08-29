package io.github.dracula101.jetscan.presentation.features.document.preview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.extensions.getReadableFileSize
import io.github.dracula101.jetscan.data.platform.utils.DateFormatter
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton


@Composable
fun PreviewImageGrid(
    scannedImage: ScannedImage,
    index: Int
) {
    Column {
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .error(R.drawable.image_error)
                .crossfade(true)
                .data(scannedImage.scannedUri)
                .build(),
            contentDescription = "Preview Image ${index + 1}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                .background(Color.White)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = scannedImage.getReadableFileSize(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(vertical = 2.dp)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (index + 1).toString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun PreviewImageListItem(
    modifier: Modifier = Modifier,
    scannedImage: ScannedImage,
    onClick: () -> Unit,
    index: Int,
) {
    val dateCreated = remember { DateFormatter.getReadableDate(scannedImage.date) }
    val fileSize = remember { scannedImage.getReadableFileSize() }
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .error(R.drawable.image_error)
                    .crossfade(true)
                    .data(scannedImage.scannedUri)
                    .build(),
                contentDescription = "Preview Image ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .heightIn(max = 100.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White),
                filterQuality = FilterQuality.Medium
            )
            Column(
                modifier = Modifier
                    .weight(2f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column {
                    Text(
                        text = "Page: ${index + 1}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "${fileSize ?: "None"} • ${scannedImage.width} x ${scannedImage.height}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .alpha(0.65f)
                    )
                }
                Text(
                    text = dateCreated,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .alpha(0.5f)
                )
            }
            CircleButton()
        }
    }
}



@Composable
fun PreviewImageGridListItem(
    modifier: Modifier = Modifier,
    scannedImage: ScannedImage,
    onClick: () -> Unit,
    index: Int,
) {
    val fileSize = remember { scannedImage.getReadableFileSize() }
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .error(R.drawable.image_error)
                .crossfade(true)
                .data(scannedImage.scannedUri)
                .build(),
            contentDescription = "Preview Image ${index + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(3 / 4f)
                .clip(MaterialTheme.shapes.small)
                .background(Color.White),
            filterQuality = FilterQuality.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = "Page: ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${fileSize}\n${scannedImage.width} x ${scannedImage.height}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .alpha(0.65f)
                )
            }
            CircleButton(
                modifier = Modifier
                    .size(30.dp)
            )
        }

    }
}
