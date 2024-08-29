package io.github.dracula101.jetscan.presentation.features.document.folder.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.platform.component.button.clickableWithoutRipple
import timber.log.Timber


@Composable
fun FolderLocation(
    path: String,
    onNavigateToFolderPath: (String, String) -> Unit
) {
    val pathParts =
        path.replace(DocumentFolder.ROOT_FOLDER, "").split("/").filter { it.isNotEmpty() }
    val scrollState = rememberScrollState()
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = 4.dp
            )
            .horizontalScroll(scrollState)
    ) {
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .clickableWithoutRipple(
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onNavigateToFolderPath(pathParts.last(), DocumentFolder.ROOT_FOLDER)
                    }
                ),
            text = "Home",
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.onSurface
        )
        pathParts.forEachIndexed { index, part ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Arrow Right",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier
                        .clickableWithoutRipple(
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                if (index == pathParts.size - 1) return@clickableWithoutRipple
                                val navigatePath = DocumentFolder.ROOT_FOLDER + "/"+ pathParts.subList(0, index + 1).joinToString("/")
                                Timber.d("Path: $navigatePath")
                                onNavigateToFolderPath(
                                    pathParts.last(),
                                    navigatePath
                                )
                            }
                        ),
                    text = part,
                    color = if (index == pathParts.size - 1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (index != pathParts.size - 1){
                        TextDecoration.Underline
                    } else {
                        TextDecoration.None
                    },
                )
            }
        }
        Spacer(modifier = Modifier.padding(start = 16.dp))
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    )
}

