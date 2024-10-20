package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.presentation.features.document.scanner.openAppSettings
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectPdfScreen(
    onNavigateBack: () -> Unit,
    documentId: String?,
    viewModel: ProtectPdfViewModel = hiltViewModel()
){

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (documentId != null){
            viewModel.trySendAction(ProtectPdfAction.Internal.LoadDocument(documentId))
        }
    }

    if (bottomSheetState.isVisible){
        DocumentFilesBottomSheet(
            onDismiss = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            },
            documents = state.value.documents,
            documentClick = {
                viewModel.trySendAction(ProtectPdfAction.Ui.SelectDocument(it))
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        )
    }

    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Protect PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        },
        bottomBar = {
            if(state.value.view == ProtectPdfView.OPERATION_VIEW){
                FilledTonalButton(
                    onClick = {
                        viewModel.trySendAction(ProtectPdfAction.Ui.ProtectPdf)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = state.value.selectedDocument != null
                ){
                    Text("Protect PDF")
                }
            }
        }
    ) { padding, windowSize->
        when(state.value.view){
            ProtectPdfView.OPERATION_VIEW -> {
                ProtectPdfView(
                    padding,
                    state,
                    onAction = { viewModel.trySendAction(it) },
                    hideBottomSheet = { coroutineScope.launch { bottomSheetState.show() } }
                )
            }
            ProtectPdfView.COMPLETED_VIEW -> {
                ProtectPdfCompletedView(
                    padding,
                    state,
                    onAction = { viewModel.trySendAction(it) }
                )
            }
        }
    }
}

@Composable
private fun ProtectPdfView(
    padding: PaddingValues,
    state: State<ProtectPdfState>,
    onAction: (ProtectPdfAction) -> Unit,
    hideBottomSheet: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val isPasswordVisible = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "Set a password to protect your scan. This password will be required if you or the person you provide the scanned document wants to access the file. If you forget the password, then this file will not be accessible forever.",
            style = MaterialTheme.typography.bodyLarge,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )
        if (state.value.selectedDocument == null) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .customContainer()
                    .clickable {
                        hideBottomSheet()
                    }
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Error",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Select a document to protect",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            DocumentTile(
                document = state.value.selectedDocument!!,
                deleteDocument = {
                    onAction(ProtectPdfAction.Ui.RemoveDocument)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.value.selectedDocument != null) {
            Column(
                modifier = Modifier
                    .alpha(if (state.value.selectedDocument == null) 0.5f else 1f)

            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Lock",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Set password",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AppTextField(
                    value = state.value.password,
                    onValueChange = { password ->
                        onAction(ProtectPdfAction.Ui.SetPassword(password))
                    },
                    label = "Password",
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isPasswordVisible.value = !isPasswordVisible.value
                            }
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible.value) Icons.Rounded.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Visibility",
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if(!isPasswordVisible.value){
                    Text(
                        state.value.password,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectPdfCompletedView(
    padding: PaddingValues,
    state: State<ProtectPdfState>,
    onAction: (ProtectPdfAction) -> Unit,
) {
    val context = LocalContext.current
    val verticalScroll = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(verticalScroll)
    ) {
        Text(
            "Your document has been protected successfully. You can now share the document with others.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .customContainer()
                .fillMaxWidth()
                .aspectRatio(3 / 4f)
        ) {
            AsyncImage(
                state.value.selectedDocument?.previewImageUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .blur(20.dp)
                    .padding(16.dp)
                    .fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = "Lock",
                    modifier = Modifier.size(65.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.value.selectedDocument?.name.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "is Protected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(
                    onClick = {
                        if (state.value.protectedPdf == null) return@ElevatedButton
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            state.value.protectedPdf!!
                        )
                        val intent = Intent(Intent.ACTION_SEND).also{
                            it.setDataAndType(uri, "application/pdf")
                            it.putExtra(Intent.EXTRA_STREAM, uri)
                            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            it.putExtra(Intent.EXTRA_SUBJECT, "Sharing from JetScan Document")
                        }
                        val activity = (context as ComponentActivity)
                        activity.startActivity(Intent.createChooser(intent, "Share"))
                    }
                ) {
                    Text(
                        "Share",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}