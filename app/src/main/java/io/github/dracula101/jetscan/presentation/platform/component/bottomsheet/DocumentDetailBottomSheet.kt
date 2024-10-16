package io.github.dracula101.jetscan.presentation.platform.component.bottomsheet

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime
import io.github.dracula101.jetscan.presentation.platform.component.document.preview.PreviewIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailBottomSheet(
    document: Document,
    onDismiss: () -> Unit,
    onAction: (DocumentAction) -> Unit,
) {
    val verticalScrollState = rememberScrollState()
    AppBottomSheet(
        onDismiss = onDismiss,
        containerColor = BottomAppBarDefaults.containerColor,
    ) {
        Column (
            modifier = Modifier
                .verticalScroll(verticalScrollState)
        ){
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(MaterialTheme.shapes.large)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewIcon(
                    uri = document.previewImageUri ?: Uri.EMPTY,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .height(70.dp)
                        .aspectRatio(3 / 4f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = document.formatDateTime(document.dateCreated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            )
            DocumentActionItem(
                title = "Save to device",
                icon = Icons.Rounded.SaveAlt,
                showArrow = true,
                onClick = { onAction(DocumentAction.SAVE_TO_DEVICE) }
            )
            DocumentActionItem(
                title = "Share",
                icon = Icons.Rounded.IosShare,
                showArrow = true,
                onClick = { onAction(DocumentAction.SHARE) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            )
            DocumentActionItem(
                title = "Add Watermark",
                resource = R.drawable.watermark_pdf,
                onClick = { onAction(DocumentAction.WATERMARK) }
            )
            DocumentActionItem(
                title = "Add Digital signature",
                resource = R.drawable.esign_pdf,
                onClick = { onAction(DocumentAction.DIGITAL_SIGNATURE) }
            )
            DocumentActionItem(
                title = "Split PDF",
                resource = R.drawable.split_pdf,
                onClick = { onAction(DocumentAction.SPLIT) }
            )
            DocumentActionItem(
                title = "Merge PDF",
                resource = R.drawable.merge_pdf,
                onClick = { onAction(DocumentAction.MERGE) }
            )
            DocumentActionItem(
                title = "Protect PDF",
                resource = R.drawable.protect_pdf,
                onClick = { onAction(DocumentAction.PROTECT) }
            )
            DocumentActionItem(
                title = "Compress PDF",
                resource = R.drawable.compress_pdf,
                onClick = { onAction(DocumentAction.COMPRESS) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            )
            DocumentActionItem(
                title = "Rename",
                icon = Icons.Rounded.Edit,
                onClick = { onAction(DocumentAction.RENAME) }
            )
            DocumentActionItem(
                title = "Print",
                icon = Icons.Rounded.Print,
                onClick = { onAction(DocumentAction.PRINT) }
            )
            DocumentActionItem(
                title = "Delete",
                icon = Icons.Rounded.Delete,
                onClick = { onAction(DocumentAction.DELETE) }
            )
        }
    }
}

@Composable
fun DocumentActionItem(
    title: String,
    icon: ImageVector? = null,
    resource: Int? = null,
    onClick: () -> Unit,
    showArrow: Boolean = false,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }else if (resource != null) {
            Icon(
                painter = painterResource(id = resource),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (showArrow){
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

enum class DocumentAction {
    SAVE_TO_DEVICE,
    SHARE,
    WATERMARK,
    DIGITAL_SIGNATURE,
    SPLIT,
    MERGE,
    PROTECT,
    COMPRESS,
    RENAME,
    PRINT,
    DELETE,
}