package io.github.dracula101.jetscan.presentation.features.tools.split_pdf


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar

@Composable
fun SplitPdfScreen(
    onNavigateBack: () -> Unit,
    documentId: String?,
    viewModel: SplitPdfViewModel = hiltViewModel()
){
    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Split PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    ) { padding, windowSize->


    }
}