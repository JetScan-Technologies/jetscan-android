package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.extensions.formatDate
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
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
            FilledTonalButton(
                onClick = {
                },
                enabled = state.value.selectedDocument != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Compress")
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
            Text(
                "Reduce the size of the PDF File"
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
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    "Select Compression level:",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                CompressionLevel.entries.map { entry ->
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
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(200.dp))

        }
    }
}