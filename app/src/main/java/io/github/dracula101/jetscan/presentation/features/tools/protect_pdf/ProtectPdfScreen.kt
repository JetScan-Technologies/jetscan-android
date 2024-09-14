package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.presentation.features.tools.merge_pdf.components.DocumentTile
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.DocumentFilesBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.customContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectPdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProtectPdfViewModel = hiltViewModel()
){

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isPasswordVisible = remember { mutableStateOf(false) }

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
            FilledTonalButton(
                onClick = {
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = state.value.selectedDocument != null
            ){
                Text("Protect PDF")
            }
        }
    ) { padding, windowSize->
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
            if (state.value.selectedDocument == null){
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
                        .height(80.dp)
                ){
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ){
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
            }else {
                DocumentTile(
                    document = state.value.selectedDocument!!,
                    deleteDocument = {
                        viewModel.trySendAction(ProtectPdfAction.Ui.RemoveDocument)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if(state.value.selectedDocument != null){
                Column(
                    modifier = Modifier
                        .alpha(if (state.value.selectedDocument == null) 0.5f else 1f)

                ){
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
                            viewModel.trySendAction(ProtectPdfAction.Ui.SetPassword(password))
                        },
                        label = "Password",
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    isPasswordVisible.value = !isPasswordVisible.value
                                }
                            ){
                                Icon(
                                    imageVector = if (!isPasswordVisible.value) Icons.Rounded.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Visibility",
                                )
                            }
                        },
                        visualTransformation = if (!isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}