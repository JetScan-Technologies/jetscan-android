package io.github.dracula101.jetscan.presentation.features.home.files_view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.WarningAmber
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder


@Composable
fun FolderItem(
    folder: DocumentFolder,
    modifier: Modifier = Modifier,
    onFolderDelete: () -> Unit,
    onClickFolder: () -> Unit = {},
    ui: FolderItemUI = FolderItemUI.Vertical
) {
    val menuExpanded = remember { mutableStateOf(false) }
    when(ui){
        is FolderItemUI.Horizontal -> {
            Row(
                modifier = modifier
                    .clip(MaterialTheme.shapes.large)
                    .customContainer(
                        shape = MaterialTheme.shapes.large
                    )
                    .clickable(onClick = onClickFolder)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painterResource(R.drawable.ic_folder),
                    contentDescription = "Folder",
                    modifier = Modifier
                        .size(65.dp)
                        .padding(4.dp)
                        .align(Alignment.CenterVertically),
                )
                Column(
                    modifier = Modifier
                        .weight(5f)
                        .padding(start = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Visible,
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Row {
                        Icon(
                            imageVector = if (folder.documentCount == 0) Icons.Rounded.WarningAmber else Icons.Outlined.Description,
                            contentDescription = "Folder",
                            modifier = Modifier.size(MaterialTheme.typography.bodySmall.fontSize.value.dp * 1.3f),
                            tint = if (folder.documentCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${folder.documentCount} File${if(folder.documentCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = if(folder.dateModified != null) "Last modified ${folder.formatDateTime(folder.dateModified)}" else "Created ${folder.formatDateTime(folder.dateCreated)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            menuExpanded.value = true
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        AppDropDown(
                            expanded = menuExpanded.value,
                            offset = DpOffset(30.dp, -(20).dp),
                            items = listOf(
                                MenuItem(
                                    "Delete",
                                    onClick = { onFolderDelete() }
                                ),
                            ),
                            onDismissRequest = { menuExpanded.value = false }
                        )
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                        )
                    }
                }
            }
        }
        is FolderItemUI.Vertical -> {
            Column(
                modifier = modifier
                    .clip(MaterialTheme.shapes.large)
                    .customContainer(
                        shape = MaterialTheme.shapes.large
                    )
                    .clickable(onClick = onClickFolder)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ){
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ){
                    Image(
                        painterResource(R.drawable.ic_folder),
                        contentDescription = "Folder",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                    )
                    if(folder.documentCount == 0){
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Folder no files",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column (
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${folder.documentCount} File${if (folder.documentCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

}


sealed class FolderItemUI {

    data object Horizontal : FolderItemUI()

    data object Vertical : FolderItemUI()

}