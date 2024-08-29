package io.github.dracula101.jetscan.presentation.platform.component.button


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.extensions.GradientColors

@Composable
fun GradientButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    showContent: Boolean = false,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    disabledColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    loadingContent: @Composable (() -> Unit)? = null,
) {

    Box(
        modifier = Modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = if (!showContent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                shape = shape
            )
            .then(
                if (showContent) Modifier
                    .background(disabledColor, shape = shape)
                else Modifier.background(
                    Brush.linearGradient(GradientColors()),
                    shape = shape
                )
            )
            .then(
                if (!showContent) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(12.dp)
            .then(modifier),
        contentAlignment = Alignment.Center
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
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
