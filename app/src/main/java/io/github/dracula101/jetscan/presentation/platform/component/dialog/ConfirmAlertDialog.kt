package io.github.dracula101.jetscan.presentation.platform.component.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onCancel: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    AppBasicDialog(
        onDismiss = onDismiss,
        title = "Select Import Quality",
        actions = {
            BasicConfirmationButton(
                positiveActionText = "OK",
                negativeActionText = "Cancel",
                onCancel = onCancel,
                onConfirm = onConfirm
            )
        }
    ) {
        content()
    }
}

@Composable
fun BasicConfirmationButton(
    positiveActionText: String,
    negativeActionText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    ElevatedButton(
        onClick = { onCancel() }
    ) {
        Text(
            negativeActionText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Spacer(modifier = Modifier.width(8.dp))
    FilledTonalButton(
        onClick = { onConfirm() }
    ) {
        Text(
            positiveActionText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}