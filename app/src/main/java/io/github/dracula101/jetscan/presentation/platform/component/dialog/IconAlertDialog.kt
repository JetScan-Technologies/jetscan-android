package io.github.dracula101.jetscan.presentation.platform.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    padding: PaddingValues = PaddingValues(16.dp),
    properties : DialogProperties = DialogProperties(),
    icon: ImageVector = Icons.Default.Warning,
    size: Dp = 120.dp,
    onConfirm: () -> Unit = {},
    onCancel: () -> Unit = {},
    confirmText: String = "OK",
    cancelText: String = "Cancel",
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .widthIn(max = 280.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(padding)
            .then(modifier),
        onDismissRequest = onDismiss,
        properties = properties,
    ){
        Surface {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Alert Icon",
                    modifier = Modifier
                        .size(size),
                    tint = MaterialTheme.colorScheme.primary
                )
                content()
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    BasicConfirmationButton(
                        negativeActionText = cancelText,
                        positiveActionText = confirmText,
                        onCancel = onCancel,
                        onConfirm = onConfirm,
                    )
                }
            }
        }
    }
}