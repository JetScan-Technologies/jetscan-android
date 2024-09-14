package io.github.dracula101.jetscan.presentation.features.tools.watermark_pdf


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffoldWithFlexAppBar

@Composable
fun WatermarkPdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: WatermarkPdfViewModel = hiltViewModel()
){
    JetScanScaffoldWithFlexAppBar(
        topAppBarTitle = "Watermark PDF",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    ) { padding, windowSize->


    }
}