@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.dracula101.jetscan.presentation.platform.component.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.AppSnackBar

@Composable
fun JetScanScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = { },
    floatingActionButton: @Composable () -> Unit = { },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    pullToRefreshState: PullToRefreshState? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    snackbarHostState: SnackbarHostState? = null,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
        .exclude(WindowInsets.statusBars)
        .exclude(WindowInsets.navigationBars),
    content: @Composable (PaddingValues, ScaffoldSize) -> Unit,
) {
    val windowWidthSize : WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val snackbarHostStateDelegate = remember { snackbarHostState }
    val dismissSnackbarState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when(it) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                    snackbarHostStateDelegate?.currentSnackbarData?.dismiss()
                }
                else-> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState true
        },
        // positional threshold of 25%
        positionalThreshold = { it * .25f }
    )
    Scaffold(
        modifier = Modifier
            .run { pullToRefreshState?.let { nestedScroll(it.nestedScrollConnection) } ?: this }
            .then(modifier),
        topBar = {
            topBar?.invoke()
        },
        bottomBar = {
            if (windowWidthSize == WindowWidthSizeClass.COMPACT) {
                bottomBar()
            }
        },
        snackbarHost = {
            if (snackbarHostStateDelegate == null) return@Scaffold
            SwipeToDismissBox(
                state = dismissSnackbarState,
                backgroundContent = {}
            ){
                SnackbarHost(
                    hostState = snackbarHostStateDelegate,
                    modifier = Modifier.padding(16.dp),
                    snackbar = { snackbarData -> AppSnackBar(snackbarData = snackbarData) }
                )
            }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .then(
                        if (topBar == null) Modifier
                            .statusBarsPadding()
                        else
                            Modifier

                    )
                    .padding(WindowInsets.ime.asPaddingValues())

            ){
                content(
                    paddingValues,
                    when(windowWidthSize) {
                        WindowWidthSizeClass.COMPACT -> ScaffoldSize.COMPACT
                        WindowWidthSizeClass.MEDIUM -> ScaffoldSize.MEDIUM
                        WindowWidthSizeClass.EXPANDED -> ScaffoldSize.EXPANDED
                        else -> ScaffoldSize.COMPACT
                    }
                )
                pullToRefreshState?.let {
                    PullToRefreshContainer(
                        state = it,
                        modifier = Modifier
                            .padding(paddingValues)
                            .align(Alignment.TopCenter),
                    )
                }
            }
        },
    )
}

enum class ScaffoldSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}