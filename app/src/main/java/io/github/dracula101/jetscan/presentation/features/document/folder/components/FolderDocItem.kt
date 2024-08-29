package io.github.dracula101.jetscan.presentation.features.document.folder.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime
import io.github.dracula101.jetscan.presentation.platform.component.document.preview.PreviewIcon
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderDocItem(
    document: Document,
    onDocumentClick: () -> Unit = {},
    onDocumentSelect: () -> Unit = {},
    onRemoveDocument: () -> Unit,
    isSelected: Boolean = true,
) {
    val menuExpanded = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(
                onClick = onDocumentClick,
                onLongClick = onDocumentSelect,
            )
            .clipToBounds()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        document.previewImageUri?.let {
            Box(
                modifier = Modifier
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                PreviewIcon(
                    uri = it,
                    modifier = Modifier
                        .offset(y = 10.dp)
                        .width(60.dp)
                        .aspectRatio(3/4f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircleOutline,
                        contentDescription = "Selected",
                        modifier = Modifier
                            .size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = document.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = document.formatDateTime(document.dateCreated),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!isSelected){
            IconButton(
                onClick = {
                    menuExpanded.value = true
                }
            ) {
                AppDropDown(
                    expanded = menuExpanded.value,
                    onDismissRequest = {
                        menuExpanded.value = false
                    },
                    offset = DpOffset(20.dp, 0.dp),
                    items = listOf(
                        MenuItem(
                            title = "Remove",
                            icon = Icons.Rounded.FolderDelete,
                            onClick = {
                                onRemoveDocument()
                            }
                        )
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More",
                )
            }
        }
    }
    HorizontalDivider(
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    )

}