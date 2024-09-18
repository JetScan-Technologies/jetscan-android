package io.github.dracula101.jetscan.presentation.features.home.home_view

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.features.home.home_view.components.CompactHomeScreen
import io.github.dracula101.jetscan.presentation.features.home.home_view.components.ExpandedHomeScreen
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeViewModel
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.ScaffoldSize
import timber.log.Timber

@Composable
fun HomeScreen(
    viewModel: MainHomeViewModel,
    windowSize: ScaffoldSize,
    padding: PaddingValues,
    onDocumentClick: (Document) -> Unit,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            Timber.d("File picked: $uri")
            viewModel.trySendAction(MainHomeAction.Alerts.ImportQualityAlert(uri))
        } else {
            viewModel.trySendAction(MainHomeAction.Alerts.FileNotSelectedAlert)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Timber.d("Permission granted: $isGranted")
            if (isGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2)
                filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
        }
    )
    when (windowSize) {
        ScaffoldSize.COMPACT -> {
            CompactHomeScreen(
                padding = padding,
                permissionLauncher = permissionLauncher,
                viewModel = viewModel,
                state = state.value,
                onDocumentClick = onDocumentClick,
            )
        }

        else -> {
            ExpandedHomeScreen(
                isExpanded = windowSize == ScaffoldSize.EXPANDED,
                padding = padding,
                permissionLauncher = permissionLauncher,
                viewModel = viewModel,
                state = state.value,
                onDocumentClick = onDocumentClick,
            )
        }
    }

}