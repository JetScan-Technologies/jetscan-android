package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.data.document.models.extensions.formatDate
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.button.GradientButton
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.pdf.models.PdfCompressionLevel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressPdfScreen(
    onNavigateBack: () -> Unit,
    documentId: String?,
    viewModel: CompressPdfViewModel = hiltViewModel()
){
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val fileActionManager = LocalFileActionManager.current

    LaunchedEffect(Unit) {
        if(documentId != null){
            viewModel.trySendAction(
                CompressPdfAction.Internal.LoadDocument(documentId)
            )
        }
    }

    if(bottomSheetState.isVisible){
        DocumentFilesBottomSheet(
            onDismiss = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            },
            documents = state.value.documents,
            documentClick = { document ->
                viewModel.trySendAction(
                    CompressPdfAction.Ui.SelectDocument(document)
                )
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        )
    }

    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Compress PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        },
        bottomBar = {
            if(state.value.pdfCompressView == PdfCompressView.DOCUMENT){
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ){
                    GradientButton(
                        text = "Compress PDF",
                        onClick = {
                            viewModel.trySendAction(CompressPdfAction.Ui.CompressPdf)
                        },
                        enabled = state.value.selectedDocument != null,
                        modifier = Modifier.fillMaxWidth(),
                        showContent = state.value.isLoading
                    )
                }
            }
        }
    ) { padding, windowSize->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ){
            when(state.value.pdfCompressView){
                PdfCompressView.DOCUMENT -> {
                    Text(
                        "Reduce the size of the PDF File. Select a PDF file to compress.",
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if(state.value.selectedDocument == null){
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .customContainer()
                                .clickable {
                                    coroutineScope.launch {
                                        bottomSheetState.show()
                                    }
                                }
                                .fillMaxWidth()
                                .height(100.dp)
                        ){
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Select a PDF file to compress"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Select a PDF file to compress"
                                )
                            }

                        }
                    }else {
                        DocumentTile(
                            document = state.value.selectedDocument!!,
                            subtitle = "${state.value.selectedDocument!!.formatDate(state.value.selectedDocument!!.dateCreated)} ${state.value.selectedDocument!!.size.bytesToReadableSize()}",
                            deleteDocument = {
                                viewModel.trySendAction(CompressPdfAction.Ui.RemoveDocument)
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .alpha(if(state.value.selectedDocument != null) 1f else 0.5f)
                    ){
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Select Compression level:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PdfCompressionLevel.entries.map { entry ->
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        enabled = state.value.selectedDocument != null
                                    ) {
                                        viewModel.trySendAction(
                                            CompressPdfAction.Ui.SelectCompressionLevel(entry)
                                        )
                                    }
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 16.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = state.value.compressionLevel == entry,
                                    onClick = {
                                        viewModel.trySendAction(
                                            CompressPdfAction.Ui.SelectCompressionLevel(entry)
                                        )
                                    },
                                    modifier = Modifier.scale(1.25f),
                                    enabled = state.value.selectedDocument != null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        entry.toFormattedString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        entry.toSubText(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                state.value.compressionSizes?.get(entry)?.let { size ->
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "${size.bytesToSizeAndUnit().first} ${size.bytesToSizeAndUnit().second}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if(state.value.compressionSizes == null){
                            ElevatedButton(
                                onClick = {
                                    viewModel.trySendAction(CompressPdfAction.Ui.GetCompressionSizes)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.value.isLoadingCompressionSizes && state.value.selectedDocument != null
                            ) {
                                if(state.value.isLoadingCompressionSizes){
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 1.5.dp
                                    )
                                }else {
                                    Text("Get Compression Sizes")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(200.dp))
                }
                PdfCompressView.COMPLETED -> {
                    val currentFileSize = state.value.selectedDocument!!.size
                    val compressedFileSize = state.value.outputFile!!.length()
                    val percentage = ((currentFileSize - compressedFileSize) / currentFileSize.toDouble()) * 100
                    Text(
                        "Compression Completed.",
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row {
                        Text(
                            "Size: ${currentFileSize.bytesToReadableSize()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 8.dp)
                        )
                        Text(
                            "  ${compressedFileSize.bytesToReadableSize()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Reduced: ${percentage.toInt()}%, Saved ${(currentFileSize - compressedFileSize).bytesToReadableSize()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .customContainer(MaterialTheme.shapes.medium),
                    ){
                        Row(
                            modifier = Modifier
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = state.value.selectedDocument?.previewImageUri,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .height(70.dp)
                                    .aspectRatio(3 / 4f)
                                    .background(MaterialTheme.colorScheme.surface)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ){
                                Text(
                                    state.value.selectedDocument?.name ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    state.value.selectedDocument?.formatDate(state.value.selectedDocument?.dateCreated ?: 0) ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.value.outputFile != null){
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val fileUri = remember {
                                    FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".provider",
                                        state.value.outputFile!!
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .clickable {
                                            fileActionManager.shareFile(
                                                fileUri,
                                                title = "Share PDF",
                                                subject = state.value.outputFile!!.name,
                                                onActivityNotFound = {}
                                            )
                                        }
                                        .padding(8.dp)
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Icon(
                                        Icons.Rounded.IosShare,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Share Compressed Pdf",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
