package io.github.dracula101.jetscan.presentation.platform.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FlatButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    showContent: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    loadingContent: @Composable (() -> Unit)? = null,
) {

    Button(
        onClick = onClick,
        enabled = !showContent && enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            contentColor = textColor,
            containerColor = containerColor,
            disabledContentColor = disabledTextColor,
            disabledContainerColor = disabledColor,
        ),
    ) {
        if (showContent) {
            Box(
                modifier = Modifier
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loadingContent!=null) loadingContent()
                else Box{}
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (enabled) textColor else disabledTextColor,
                ),
            )
        }
    }
}



@Composable
fun FlatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFilled: Boolean = false,
    text: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                color = if (isFilled) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(
                onClick = onClick,
            )
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        text()
    }
}
