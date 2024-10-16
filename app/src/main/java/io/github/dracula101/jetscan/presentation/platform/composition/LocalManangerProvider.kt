package io.github.dracula101.jetscan.presentation.platform.composition

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import io.github.dracula101.jetscan.data.platform.manager.biometrics.BiometricsManager
import io.github.dracula101.jetscan.data.platform.manager.biometrics.BiometricsManagerImpl
import io.github.dracula101.jetscan.data.platform.manager.exit.ExitManager
import io.github.dracula101.jetscan.data.platform.manager.exit.ExitManagerImpl
import io.github.dracula101.jetscan.data.platform.manager.file_action.FileActionManager
import io.github.dracula101.jetscan.data.platform.manager.file_action.FileActionManagerImpl
import io.github.dracula101.jetscan.data.platform.manager.permission.PermissionsManager
import io.github.dracula101.jetscan.data.platform.manager.permission.PermissionsManagerImpl

@Composable
fun LocalManagerProvider(content: @Composable () -> Unit) {
    val activity = LocalContext.current as Activity
    CompositionLocalProvider(
        LocalPermissionsManager provides PermissionsManagerImpl(activity),
        LocalExitManager provides ExitManagerImpl(activity),
        LocalBiometricsManager provides BiometricsManagerImpl(activity),
        LocalFileActionManager provides FileActionManagerImpl(activity)
    ) {
        content()
    }
}

/**
 * Provides access to the biometrics manager throughout the app.
 */
val LocalBiometricsManager: ProvidableCompositionLocal<BiometricsManager> = compositionLocalOf {
    error("CompositionLocal BiometricsManager not present")
}

/**
 * Provides access to the exit manager throughout the app.
 */
val LocalExitManager: ProvidableCompositionLocal<ExitManager> = compositionLocalOf {
    error("CompositionLocal ExitManager not present")
}

/**
 * Provides access to the permission manager throughout the app.
 */
val LocalPermissionsManager: ProvidableCompositionLocal<PermissionsManager> = compositionLocalOf {
    error("CompositionLocal LocalPermissionsManager not present")
}

val LocalFileActionManager: ProvidableCompositionLocal<FileActionManager> = compositionLocalOf {
    error("CompositionLocal LocalFileActionManager not present")
}