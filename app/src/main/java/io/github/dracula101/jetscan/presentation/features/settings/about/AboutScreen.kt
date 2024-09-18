package io.github.dracula101.jetscan.presentation.features.settings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.presentation.features.settings.about.components.AboutScreenItem
import io.github.dracula101.jetscan.presentation.features.settings.about.components.AboutTopAppBar
import io.github.dracula101.jetscan.presentation.features.settings.about.components.DeveloperInfoBottomSheet
import io.github.dracula101.jetscan.presentation.features.settings.about.components.JetScanFooter
import io.github.dracula101.jetscan.presentation.features.settings.about.components.JetScanLogo
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOpenSourceLibraries: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val developerBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    if (developerBottomSheetState.isVisible) {
         DeveloperInfoBottomSheet(
             onDismiss = {
                 coroutineScope.launch {
                     developerBottomSheetState.hide()
                 }
             }
         )
    }

    JetScanScaffold(
        topBar = {
            AboutTopAppBar(
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues, size ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                JetScanLogo(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                        .size(140.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "v${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            AboutScreenItem(
                title = "Developer",
                onClick = {
                    coroutineScope.launch {
                        developerBottomSheetState.show()
                    }
                }
            )
            AboutScreenItem(
                title = "Privacy Policy",
                onClick = {}
            )
            AboutScreenItem(
                title = "Terms of Service",
                onClick = {}
            )
            AboutScreenItem(
                title = "Rate JetScan",
                onClick = {}
            )
            AboutScreenItem(
                title = "Open Source Licenses",
                onClick = { onNavigateToOpenSourceLibraries() }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            JetScanFooter()
        }
    }
}
