@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.dracula101.jetscan.presentation.platform.component.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetScanTopAppbar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    scrollBehavior: TopAppBarScrollBehavior,
    navigationIcon: (@Composable () -> Unit)? = null,
    navigationIconContentDescription: String = "",
    onNavigationIconClick: () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = { },
) {
    TopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            navigationIcon?.let {
                IconButton(
                    onClick = onNavigationIconClick,
                    content = navigationIcon,
                )
            } ?: Unit
        },
        title = title,
        modifier = modifier
            .testTag("HeaderBarComponent"),
        actions = actions,
    )
}

@Composable
fun BackButtonIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
        contentDescription = "Back",
        modifier = Modifier.offset(x = 4.dp)
    )
}