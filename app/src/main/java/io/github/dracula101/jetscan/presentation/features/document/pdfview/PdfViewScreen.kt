package io.github.dracula101.jetscan.presentation.features.document.pdfview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.SaveDocumentBottomSheet
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.SaveOption
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.text.FittedText
import io.github.dracula101.pdf.ui.PdfReader
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
    val saveInternalStorageActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val document = state.value.document
                if (document != null) {
                    val contentResolver = context.contentResolver
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        contentResolver.openInputStream(document.uri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        }
    }


    if (showSaveBottomSheet.isVisible && state.value.document != null) {
        SaveDocumentBottomSheet(
            document = state.value.document!!,
            onDismiss = {
                coroutineScope.launch {
                    showSaveBottomSheet.hide()
                }
            },
            selectSaveOption = { saveOption ->
                when (saveOption) {
                    SaveOption.INTERNAL_STORAGE -> {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_TITLE, state.value.document!!.name)
                        }
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        saveInternalStorageActivityLauncher.launch(intent)
                    }
                    SaveOption.GOOGLE_DRIVE -> {
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            state.value.document!!.uri.toFile()
                        )
                        val activity = (context as ComponentActivity)
                        activity.shareToApp(
                            uri = uri,
                            packageName = "com.google.android.apps.docs",
                            mimeType = "application/pdf",
                            subject = documentName ?: "JetScan Document",
                            chooserTitle = "Share"
                        )
                    }
                    SaveOption.EMAIL -> {
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            state.value.document!!.uri.toFile()
                        )
                        val activity = (context as ComponentActivity)
                        activity.shareToApp(
                            uri = uri,
                            packageName = "com.google.android.gm",
                            mimeType = "application/pdf",
                            subject = "Sharing ${documentName ?: "JetScan Document"}",
                            chooserTitle = "Share"
                        )
                    }
                    SaveOption.WHATSAPP -> {
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            state.value.document!!.uri.toFile()
                        )
                        val activity = (context as ComponentActivity)
                        activity.shareToApp(
                            uri = uri,
                            packageName = "com.whatsapp",
                            mimeType = "application/pdf",
                            subject = "Sharing from JetScan Document",
                            chooserTitle = "Share"
                        )
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.trySendAction(PdfAction.Internal.LoadPdf(documentId))
    }
    JetScanScaffold(
        topBar = {
            JetScanTopAppbar(
                title = {
                    Text(
                        text = documentName ?: "JetScan Document",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = onNavigateBack
            )
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
            Row {
                PdfReader(
                    file = state.value.document!!.uri.toFile(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(padding),
                    showMarker = false
                )
                if(widthAnimation.value != 0f){
                    Box(
                        modifier = Modifier
                            .offset(x = (100 * (1 - widthAnimation.value)).dp)
                            .width(IntrinsicSize.Min)
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
                                    val intent = Intent(Intent.ACTION_SEND).also{
                                        it.setDataAndType(uri, "application/pdf")
                                        it.putExtra(Intent.EXTRA_STREAM, uri)
                                        it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        it.putExtra(Intent.EXTRA_SUBJECT, "Sharing from JetScan Document")
                                    }
                                    val activity = (context as ComponentActivity)
                                    activity.startActivity(Intent.createChooser(intent, "Share"))
                                },
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
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
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Print",
                                icon = Icons.Rounded.Print,
                                onClick = {

                                },
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
                            )
                            HorizontalDivider()
                            PdfActionTitle(
                                title = "Open in",
                                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                onClick = {
                                },
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
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
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
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
                                modifier = Modifier.padding(vertical = 8.dp ,horizontal = 4.dp)
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


fun Activity.shareToApp(
    uri: Uri,
    packageName: String,
    mimeType: String,
    subject: String,
    chooserTitle: String,
    onActivityNotFound: () -> Unit = {}
){
    val intent = Intent(Intent.ACTION_SEND).also {
        it.setPackage(packageName)
        it.setDataAndType(uri, mimeType)
        it.putExtra(Intent.EXTRA_STREAM, uri)
        it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        it.putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (e: Exception) {
        onActivityNotFound()
    }
}