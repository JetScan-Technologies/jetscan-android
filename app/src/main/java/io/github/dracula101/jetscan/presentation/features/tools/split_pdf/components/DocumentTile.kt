package io.github.dracula101.jetscan.presentation.features.tools.split_pdf.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer

@Composable
fun DocumentTile(
    document: Document,
    subtitle: String? = null,
    deleteDocument: ((Document) -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .customContainer(MaterialTheme.shapes.large)
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(start = 16.dp, end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = document.previewImageUri,
                contentDescription = document.name,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .height(80.dp)
                    .aspectRatio(3/4f)
                    .background(Color.White)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle ?: document.formatDateTime(document.dateCreated),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = document.scannedImages.size.toString() + " pages",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (deleteDocument != null) {
                IconButton(
                    onClick = { deleteDocument(document) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }

}