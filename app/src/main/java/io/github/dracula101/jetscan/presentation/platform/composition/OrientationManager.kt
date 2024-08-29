package io.github.dracula101.jetscan.presentation.platform.composition

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockOrientation(
    isPortraitOnly: Boolean = false,
    isLandscapeOnly: Boolean = false,
    isBoth: Boolean = true,
    content: @Composable () -> Unit
) {
    val orientation = when {
        isPortraitOnly -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        isLandscapeOnly -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        isBoth -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
    content()
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}