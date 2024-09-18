package io.github.dracula101.jetscan.presentation.features.document.pdfview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DriveFolderUpload
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.extensions.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveDocumentBottomSheet(
    document: Document,
    onDismiss: () -> Unit,
    selectSaveOption: (SaveOption) -> Unit,
){
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                "Save Document",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                document.name,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Created at ${document.formatDateTime(document.dateCreated)}",
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SaveOption.entries.size) { index ->
                    SaveOptionItem(
                        saveOption = SaveOption.entries[index],
                        onClick = {
                            selectSaveOption(SaveOption.entries[index])
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun SaveOptionItem(
    saveOption: SaveOption,
    onClick: () -> Unit,
    iconSize: Dp = 50.dp
){
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when(saveOption){
            SaveOption.INTERNAL_STORAGE -> Icon(
                imageVector = Icons.Rounded.DriveFolderUpload,
                contentDescription = "Save to internal storage",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            SaveOption.GOOGLE_DRIVE -> Icon(
                painter = painterResource(id = R.drawable.drive_export),
                contentDescription = "Save to Google Drive",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            SaveOption.EMAIL ->  Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = "Save to Email",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            SaveOption.WHATSAPP -> Icon(
                painter = painterResource(id = R.drawable.whatsapp_dark),
                contentDescription = "Save to WhatsApp",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            saveOption.toFormattedString(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

enum class SaveOption {
    INTERNAL_STORAGE,
    GOOGLE_DRIVE,
    EMAIL,
    WHATSAPP,;

    fun toFormattedString(): String {
        return when(this){
            INTERNAL_STORAGE -> "Internal Storage"
            GOOGLE_DRIVE -> "Google Drive"
            EMAIL -> "Email"
            WHATSAPP -> "WhatsApp"
        }
    }
}