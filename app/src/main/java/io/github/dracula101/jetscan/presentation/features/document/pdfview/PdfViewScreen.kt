package io.github.dracula101.jetscan.presentation.features.document.pdfview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.PdfActionTitle
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.PdfDocumentAdapter
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.SaveDocumentBottomSheet
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.SaveOption
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.text.FittedText
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.pdf.ui.PdfReader
import io.github.dracula101.pdf.ui.rememberPdfTransformState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewScreen(
    documentId: String,
    documentName: String?,
    viewModel: PdfViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    compressDocument: (Document) -> Unit,
    mergeDocument: (Document) -> Unit,
    protectDocument: (Document) -> Unit,
    splitDocument: (Document) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val widthAnimation = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val showSaveBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val pdfLazyListState = rememberLazyListState()
    val fileActionManager = LocalFileActionManager.current
    val saveToStorageLauncher = fileActionManager.saveFileWithLauncher { state.value.document!!.uri }

    if (showSaveBottomSheet.isVisible && state.value.document != null) {
        SaveDocumentBottomSheet(
            document = state.value.document!!,
            onDismiss = {
                coroutineScope.launch {
                    showSaveBottomSheet.hide()
                }
            },
            selectSaveOption = { saveOption ->
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    state.value.document!!.uri.toFile()
                )
                when (saveOption) {
                    SaveOption.INTERNAL_STORAGE -> {
                        val intent = fileActionManager.saveFileIntent(
                            uri = uri,
                            title = documentName ?: "JetScan Document"
                        )
                        saveToStorageLauncher.launch(intent)
                    }
                    SaveOption.GOOGLE_DRIVE -> {
                        fileActionManager.shareToGDrive(
                            uri = uri,
                            subject = "Sharing ${documentName ?: "JetScan Document"}",
                            activityNotFound = {},
                        )
                    }
                    SaveOption.EMAIL -> {
                        fileActionManager.shareToEmail(
                            uri = uri,
                            subject = "Sharing ${documentName ?: "JetScan Document"}",
                            activityNotFound = {},
                        )
                    }
                    SaveOption.WHATSAPP -> {
                        fileActionManager.shareToWhatsapp(
                            uri = uri,
                            subject = "Sharing ${documentName ?: "JetScan Document"}",
                            activityNotFound = {},
                        )
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.trySendAction(PdfViewAction.Internal.LoadPdfView(documentId))
    }
    JetScanScaffold(
        topBar = {
            if(widthAnimation.value != 0f){
                JetScanTopAppbar(
                    title = {
                        Text(
                            text = documentName ?: "JetScan Document",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    onNavigationIconClick = onNavigateBack,
                    modifier = Modifier
                        .offset(y = -(150 * (1 - widthAnimation.value)).dp)
                )
            }
        },
        floatingActionButton = {
            if(widthAnimation.value == 0f){
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = {
                        coroutineScope.launch {
                            widthAnimation.animateTo(1f)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Open"
                    )
                }
            }
        }
    ) { padding,_->
        if(state.value.document != null){
            val pdfTransformState = rememberPdfTransformState(file = state.value.document!!.uri.toFile(), lazyListState = pdfLazyListState)
            val isZoomed = remember { mutableStateOf(false) }
            LaunchedEffect(Unit){
                snapshotFlow { pdfTransformState.scale }
                    .collect {
                        if(it > 1.0f && !isZoomed.value){
                            isZoomed.value = true
                        } else if(it == 1.0f && isZoomed.value){
                            isZoomed.value = false
                        }
                    }
            }
            LaunchedEffect(isZoomed.value){
                coroutineScope.launch {
                    widthAnimation.animateTo(
                        if(isZoomed.value) 0f else 1f,
                        animationSpec = tween(300)
                    )
                }
            }

            Row {
                PdfReader(
                    file = state.value.document!!.uri.toFile(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(padding),
                    lazyListState = pdfLazyListState,
                    pdfTransformState = pdfTransformState,
                    showMarker = false
                )
                if(widthAnimation.value != 0f){
                    Box(
                        modifier = Modifier
                            .offset(x = (100 * (1 - widthAnimation.value)).dp)
                            .width(58.dp)
                            .fillMaxHeight()
                            .padding(padding)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxHeight()
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            PdfActionTitle(
                                title = "Share",
                                icon = Icons.Rounded.IosShare,
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".provider",
                                        state.value.document!!.uri.toFile()
                                    )
                                    fileActionManager.shareFile(
                                        uri = uri,
                                        title = state.value.document?.name ?: "JetScan Document",
                                        subject = "Share PDF",
                                        onActivityNotFound = {}
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Save",
                                icon = Icons.Rounded.SaveAlt,
                                onClick = {
                                    coroutineScope.launch {
                                        showSaveBottomSheet.expand()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Print",
                                icon = Icons.Rounded.Print,
                                onClick = {
                                    fileActionManager.shareToPrinter(
                                        file = state.value.document!!.uri.toFile(),
                                        subject = "Printing ${state.value.document?.name ?: ""} Document",
                                        activityNotFound = {}
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Open in",
                                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".provider",
                                        state.value.document!!.uri.toFile()
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Compress",
                                icon = Icons.Rounded.Compress,
                                onClick = {
                                    if (state.value.document != null){
                                        compressDocument(state.value.document!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Merge",
                                icon = Icons.Rounded.Merge,
                                onClick = {
                                    if (state.value.document != null){
                                        mergeDocument(state.value.document!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        ),
                                        endY = 110f,
                                    ),
                                )
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        widthAnimation.animateTo(0f)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = "Close"
                                )
                            }
                            FittedText(
                                text = state.value.document!!.size.bytesToSizeAndUnit().first.toString(),
                                fontWeight = FontWeight.ExtraLight,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                            )
                            Text(
                                state.value.document!!.size.bytesToSizeAndUnit().second,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        }
    }
}