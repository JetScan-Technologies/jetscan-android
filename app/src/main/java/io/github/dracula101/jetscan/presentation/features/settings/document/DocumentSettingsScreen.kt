package io.github.dracula101.jetscan.presentation.features.settings.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CropOriginal
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Margin
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.presentation.features.settings.document.components.DocumentSettingTopAppBar
import io.github.dracula101.jetscan.presentation.features.settings.document.components.SettingListTile
import io.github.dracula101.jetscan.presentation.features.settings.document.components.SettingSwitchTile
import io.github.dracula101.jetscan.presentation.features.settings.document.components.SettingTextfieldTile
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.MaxDocumentSize
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSettingsScreen(
    onNavigateBack: () -> Unit,
    screen: DocumentSettingScreen,
    viewModel: DocumentSettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val topAppbarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    JetScanScaffold(
        topBar = {
            DocumentSettingTopAppBar(
                onNavigateBack = onNavigateBack,
                screen = screen,
                appBarBehavior = topAppbarBehavior
            )
        },
        bottomBar = {
            if(screen == DocumentSettingScreen.DOC_CONFIG){
                DocumentNamePreview(
                    state = state.value
                )
            }
        },
        alwaysShowBottomBar = true,
    ) {padding, size->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
        ){
            when(screen){
                DocumentSettingScreen.IMPORT_EXPORT -> ImportExportScreen(
                    state = state.value,
                    onAction = viewModel::trySendAction
                )
                DocumentSettingScreen.PDF_SETTINGS -> PdfSettingsScreen(
                    state = state.value,
                    onAction = viewModel::trySendAction
                )
                DocumentSettingScreen.DOC_CONFIG -> DocConfigScreen(
                    state = state.value,
                    onAction = viewModel::trySendAction
                )
                DocumentSettingScreen.CAMERA_CONFIG -> CameraConfigScreen(
                    state = state.value,
                    onAction = viewModel::trySendAction
                )
            }
        }
    }
}

@Composable
fun ColumnScope.ImportExportScreen(
    state: DocumentSettingsState,
    onAction: (DocumentSettingsAction) -> Unit
) {
    // Default Quality
    SettingListTile(
        title = "Import/Export Quality",
        description = "Default quality for imported and exported images",
        leading = {
            Icon(
                imageVector = Icons.Rounded.HighQuality,
                contentDescription = "Quality"
            )
        },
        menuItems = ImageQuality.entries.map { quality ->
            MenuItem(
                title = quality.toFormattedString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.ImportExportQuality.ChangeImportExportQuality(quality))
                }
            )
        },
        currentItem = state.importExportQuality.toFormattedString()
    )
    SettingSwitchTile(
        title = "Allow Image",
        description = "Allow images for import",
        leading = {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = "Image"
            )
        },
        checked = state.allowImageForImport,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.ImportExportQuality.ChangeAllowImageForImport)
        }
    )
    SettingListTile(
        title = "Max Document Size",
        description = "Maximum size of the document for import",
        leading = {
            Icon(
                imageVector = Icons.Rounded.PictureAsPdf,
                contentDescription = "PDF"
            )
        },
        menuItems = MaxDocumentSize.entries.map { size ->
            MenuItem(
                title = size.toString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.ImportExportQuality.ChangeMaxDocumentSize(size))
                }
            )
        },
        currentItem = state.maxDocumentSize.toString()
    )
    SettingSwitchTile(
        title = "Use app naming convention",
        description = "Use the app naming convention for imported documents",
        leading = {
            Icon(
                imageVector = Icons.Rounded.FileOpen,
                contentDescription = "File"
            )
        },
        checked = state.useAppNamingConvention,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.ImportExportQuality.ChangeUseAppNamingConvention)
        }
    )
    SettingSwitchTile(
        title = "Avoid Password Protected Pdf",
        description = "Avoid password protected pdf files for import",
        leading = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Locked"
            )
        },
        checked = state.avoidPasswordProtectionFiles,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.ImportExportQuality.ChangeAvoidPasswordProtectionFiles)
        }
    )
}

@Composable
fun ColumnScope.PdfSettingsScreen(
    state: DocumentSettingsState,
    onAction: (DocumentSettingsAction) -> Unit
) {
    SettingListTile(
        title = "Quality",
        description = "Default quality for pdf images during scanning",
        leading = {
            Icon(
                imageVector = Icons.Rounded.HighQuality,
                contentDescription = "Quality"
            )
        },
        menuItems = PdfQuality.entries.map {
            MenuItem(
                title = it.toString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.PdfSettings.ChangePdfQuality(it))
                }
            )
        },
        currentItem = state.pdfQuality.toString()
    )
    SettingListTile(
        title = "Page Size",
        description = "Size of the pdf page during scanning",
        leading = {
            Icon(
                imageVector = Icons.Rounded.PhotoSizeSelectLarge,
                contentDescription = "Size"
            )
        },
        menuItems = PdfPageSize.entries.map {
            MenuItem(
                title = it.toString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.PdfSettings.ChangePdfPageSize(it))
                }
            )
        },
        currentItem = state.pdfPageSize.toString()
    )
    SettingSwitchTile(
        title = "Margin",
        description = "Add margin to the pdf page",
        leading = {
            Icon(
                imageVector = Icons.Rounded.Margin,
                contentDescription = "Margin"
            )
        },
        checked = state.hasPdfMargin,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.PdfSettings.ChangePdfMargin)
        }
    )
    SettingSwitchTile(
        title = "Auto Crop",
        description = "Crop the image automatically after scanning",
        leading = {
            Icon(
                imageVector = Icons.Rounded.CropOriginal,
                contentDescription = "Auto Crop"
            )
        },
        checked = state.hasAutoCrop,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.PdfSettings.ChangeAutoCrop)
        }
    )
}

@Composable
fun DocumentNamePreview(
    state: DocumentSettingsState
) {
    val calendar = remember { Calendar.getInstance() }
    val date = remember { mutableStateOf(calendar.time) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .customContainer(shape = MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text("Preview")
            Text(
                "${state.documentPrefix} " +
                        (if (state.documentHasDate) "${state.documentDatePattern.format(date.value)} " else "") +
                        (if (state.documentHasTime) "${state.documentTimePattern.format(date.value)} " else "") +
                        (state.documentSuffix ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ColumnScope.DocConfigScreen(
    state: DocumentSettingsState,
    onAction: (DocumentSettingsAction) -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "Document Name Settings",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    SettingTextfieldTile(
        title = "Prefix",
        labelText = "Enter the prefix for the document",
        leading = {
            Row {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Prefix"
                )
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        value = state.documentPrefix,
        onValueChange = {
            onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentPrefix(it))
        }
    )
    SettingTextfieldTile(
        title = "Suffix",
        labelText = "Enter the suffix for the document",
        leading = {
            Row {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Suffix"
                )
            }
        },
        value = state.documentSuffix ?: "",
        onValueChange = {
            onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentSuffix(it))
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.SubdirectoryArrowRight,
            contentDescription = "Date"
        )
        Icon(
            imageVector = Icons.Rounded.DateRange,
            contentDescription = "Date",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Date",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    SettingSwitchTile(
        title = "Date",
        description = "Add date to the document name",
        checked = state.documentHasDate,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentHasDate)
        }
    )

    SettingListTile(
        title = "Pattern",
        description = "Select the date pattern for the document name",
        menuItems = DocumentDatePattern.entries.map {
            MenuItem(
                title = it.toString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentDatePattern(it))
                }
            )
        },
        currentItem = state.documentDatePattern.toString(),
        modifier = Modifier.then(
            if (!state.documentHasDate) Modifier.alpha(0.35f) else Modifier
        ),
        enabled = state.documentHasDate
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.SubdirectoryArrowRight,
            contentDescription = "Time"
        )
        Icon(
            imageVector = Icons.Rounded.AccessTime,
            contentDescription = "Time",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Time",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    SettingSwitchTile(
        title = "Time",
        description = "Add time to the document name",
        checked = state.documentHasTime,
        onCheckedChange = {
            onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentHasTime)
        }
    )
    SettingListTile(
        title = "Pattern",
        description = "Select the time pattern for the document name",
        menuItems = DocumentTimePattern.entries.map {
            MenuItem(
                title = it.toString(),
                onClick = {
                    onAction(DocumentSettingsAction.Ui.DocConfig.ChangeDocumentTimePattern(it))
                }
            )
        },
        currentItem = state.documentTimePattern.toString(),
        modifier = Modifier.then(
            if (!state.documentHasTime) Modifier.alpha(0.35f) else Modifier
        ),
        enabled = state.documentHasTime
    )

}

@Composable
fun ColumnScope.CameraConfigScreen(
    state: DocumentSettingsState,
    onAction: (DocumentSettingsAction) -> Unit
) {

}