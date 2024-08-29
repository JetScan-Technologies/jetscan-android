package io.github.dracula101.jetscan.presentation.features.settings.document

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSettingsScreen(
    onNavigateBack: () -> Unit
) {
    JetScanScaffold(
        topBar = {
            DocumentSettingTopAppBar(
                onNavigateBack = onNavigateBack
            )
        }
    ) {padding, size->

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSettingTopAppBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Document Settings",
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