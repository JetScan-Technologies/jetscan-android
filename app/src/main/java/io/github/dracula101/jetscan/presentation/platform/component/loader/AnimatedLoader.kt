package io.github.dracula101.jetscan.presentation.platform.component.loader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedLoader(
    modifier: Modifier = Modifier,
    value: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeCap: StrokeCap = StrokeCap.Round,
    height: Dp = 12.dp,
) {
    assert(value in 0f..1f)
    var progress by remember { mutableFloatStateOf(0f) }
    val progressAnimDuration = 1000
    val progressAnimation by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = progressAnimDuration, easing = FastOutSlowInEasing),
        label = "Animated Loader"
    )
    LaunchedEffect(value) {
        progress = value
    }
    LinearProgressIndicator(
        progress = { progressAnimation },
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .height(height),
        color = color,
        strokeCap = strokeCap,
    )
}

@Preview
@Composable
fun AnimatedLoaderPreview() {
    val progress = remember { mutableStateOf(0.4f) }
    LaunchedEffect(Unit) {
        progress.value = 0.4f
        delay(1000)
        progress.value = 0.8f
    }
    AnimatedLoader(
        value = progress.value,
    )
}