package io.github.dracula101.jetscan.presentation.platform.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBasicDialog(
    modifier: Modifier = Modifier,
    title: String,
    onDismiss: () -> Unit = {},
    padding: PaddingValues = PaddingValues(16.dp),
    titlePadding: PaddingValues = PaddingValues(start = 8.dp),
    properties : DialogProperties = DialogProperties(),
    actions: @Composable (RowScope.() -> Unit) = {},
    content: @Composable () -> Unit,
 ) {
    BasicAlertDialog(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(padding)
            .then(modifier),
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        Surface {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(titlePadding)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                ){ content() }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { actions() }
            }
        }
    }
}