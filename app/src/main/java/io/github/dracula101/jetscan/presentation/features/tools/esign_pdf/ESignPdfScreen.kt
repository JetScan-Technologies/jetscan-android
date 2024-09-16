package io.github.dracula101.jetscan.presentation.features.tools.esign_pdf


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar

@Composable
fun ESignPdfScreen(
    onNavigateBack: () -> Unit,
    documentId : String?,
    viewModel: ESignPdfViewModel = hiltViewModel()
){
    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "ESign PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    ) { padding, windowSize->


    }
}