package io.github.dracula101.jetscan.presentation.features.settings.document.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import io.github.dracula101.jetscan.presentation.features.settings.document.DocumentSettingScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSettingTopAppBar(
    onNavigateBack: () -> Unit,
    screen: DocumentSettingScreen,
    appBarBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = screen.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        scrollBehavior = appBarBehavior,
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