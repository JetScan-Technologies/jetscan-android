package io.github.dracula101.jetscan.presentation.platform.base.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Convenience method for observing event flow from [BaseViewModel].
 */
@Composable
fun <E> EventsEffect(
    viewModel: BaseViewModel<*, E, *>,
    handler: suspend (E) -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow
            .onEach { handler.invoke(it) }
            .launchIn(this)
    }
}
