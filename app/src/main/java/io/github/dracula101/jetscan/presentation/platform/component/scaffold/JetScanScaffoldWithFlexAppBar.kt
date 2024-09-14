package io.github.dracula101.jetscan.presentation.platform.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetScanScaffoldWithFlexAppBar(
    modifier: Modifier = Modifier,
    topAppBarTitle: String,
    actions:  @Composable() (RowScope.() -> Unit) = {},
    navigationIcon: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = { },
    floatingActionButton: @Composable () -> Unit = { },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    snackbarHostState: SnackbarHostState? = null,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
        .exclude(WindowInsets.statusBars)
        .exclude(WindowInsets.navigationBars),
    content: @Composable (PaddingValues, ScaffoldSize) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val topBarAppScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    JetScanScaffold(
        modifier = modifier.then(
            Modifier.nestedScroll(topBarAppScrollBehavior.nestedScrollConnection)
        ),
        topBar = {
            MediumTopAppBar(
                scrollBehavior = topBarAppScrollBehavior,
                title = {
                    Text(
                        text = topAppBarTitle,
                        style = if (topBarAppScrollBehavior.state.collapsedFraction > 0.5f) {
                            MaterialTheme.typography.bodyLarge
                        } else {
                            MaterialTheme.typography.headlineLarge
                        }
                    )
                },
                navigationIcon = navigationIcon,
                actions = actions
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        snackbarHostState = snackbarHostState,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}