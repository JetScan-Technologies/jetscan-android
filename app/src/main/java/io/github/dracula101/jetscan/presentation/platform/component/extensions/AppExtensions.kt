package io.github.dracula101.jetscan.presentation.platform.component.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Modifier.gradientContainer(
    shape: Shape = MaterialTheme.shapes.large,
    colors: List<Color> = GradientColors(),
    borderWidth: Dp = 1.dp,
) = this
    .clip(shape)
    .border(borderWidth, MaterialTheme.colorScheme.primary, shape)
    .background(Brush.linearGradient(colors))



val GradientColors : @Composable () -> List<Color> = {
    listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    )
}