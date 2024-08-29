package io.github.dracula101.jetscan.presentation.features.settings.open_source_libs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLibrariesScreen(
    onNavigateBack: () -> Unit
) {
    JetScanScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Open Source Libraries",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateBack() }
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {padding, size->
        LibrariesContainer(
            Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}