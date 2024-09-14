package io.github.dracula101.jetscan.presentation.features.document.pdfview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.pdf.ui.PdfReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewScreen(
    documentId: String,
    documentName: String?,
    viewModel: PdfViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
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
        }
    ) { padding,_->
        if(state.value.document != null){
            PdfReader(
                file = state.value.document!!.uri.toFile(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}