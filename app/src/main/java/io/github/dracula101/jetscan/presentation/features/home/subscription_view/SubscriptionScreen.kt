package io.github.dracula101.jetscan.presentation.features.home.subscription_view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize

@Composable
fun SubscriptionScreen(
    windowSize: ScaffoldSize,
    viewModel: SubscriptionViewModel,
    padding: PaddingValues,
    mainHomeState: MainHomeState,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.padding(padding)
    ) {
        item {
            Text("Subscription Screen")
        }
    }
}