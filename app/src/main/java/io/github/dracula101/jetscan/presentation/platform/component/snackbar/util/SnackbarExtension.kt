package io.github.dracula101.jetscan.presentation.platform.component.snackbar.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.ErrorSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.SuccessSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.WarningSnackbar

suspend fun SnackbarHostState.showWarningSnackbar(
    message: String,
    detail: String? = null,
    actionLabel: String? = null,
    onDismiss: () -> Unit = {},
) {
    if (currentSnackbarData != null) {
        currentSnackbarData?.dismiss()
    }
    val snackbarResult = showSnackbar(
        visuals = WarningSnackbar(
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
            message = message,
            details = detail,
            withDismissAction = true
        )
    )
    when (snackbarResult) {
        SnackbarResult.Dismissed, SnackbarResult.ActionPerformed -> onDismiss()
    }
}

suspend fun SnackbarHostState.showErrorSnackBar(
    message: String,
    detail: String? = null,
    errorCode: String? = null,
    actionLabel: String? = null,
    onDismiss: () -> Unit,
) {
    if (currentSnackbarData != null) {
        currentSnackbarData?.dismiss()
    }
    val snackbarResult = showSnackbar(
        visuals = ErrorSnackbar(
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
            message = message,
            details = detail,
            errorCode = errorCode,
            withDismissAction = true
        )
    )
    when (snackbarResult) {
        SnackbarResult.Dismissed, SnackbarResult.ActionPerformed -> onDismiss()
    }
}


suspend fun SnackbarHostState.showSuccessSnackbar(
    message: String,
    onDismiss: () -> Unit,
) {
    if (currentSnackbarData != null) {
        currentSnackbarData?.dismiss()
    }
    val snackbarResult = showSnackbar(
        visuals = SuccessSnackbar(
            duration = SnackbarDuration.Short,
            message = message,
        )
    )
    when (snackbarResult) {
        SnackbarResult.Dismissed, SnackbarResult.ActionPerformed -> onDismiss()
    }
}