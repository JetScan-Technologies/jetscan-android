package io.github.dracula101.jetscan.presentation.features.settings.about.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutTopAppBar(
    onNavigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = "About JetScan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}