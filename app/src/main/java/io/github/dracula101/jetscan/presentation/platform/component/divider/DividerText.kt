package io.github.dracula101.jetscan.presentation.platform.component.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun DividerText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    style : TextStyle = MaterialTheme.typography.bodySmall,
    padding: PaddingValues = PaddingValues(0.dp),
    alignment: Alignment = Alignment.Center
) {
    Box(
        contentAlignment = alignment,
    ){
        HorizontalDivider(
            color = color.copy(alpha = 0.5f),
            modifier = Modifier.padding(padding)
        )
        Box (
            modifier = Modifier
                .background(backgroundColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = text,
                style = style,
                color = color,
            )
        }
    }
}