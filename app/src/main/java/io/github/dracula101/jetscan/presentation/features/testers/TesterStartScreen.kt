package io.github.dracula101.jetscan.presentation.features.testers

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun TesterStartScreen(
    navigateToNextScreen: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Repeat Hand Animation")
    val repeatableAnimation = infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                delayMillis = 1000
            ),
            initialStartOffset = StartOffset(
                offsetType = StartOffsetType.Delay,
                offsetMillis = 30
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Repeat Hand Animation"
    )
    JetScanScaffold(
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        navigateToNextScreen()
                    }.background(MaterialTheme.colorScheme.primary)
                    .fillMaxWidth()
                    .padding(12.dp)
            ){
                Text(
                    text = "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

        },
        alwaysShowBottomBar = true
    ) { padding, windowSize->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically)
        ) {
            Text(
                "\uD83D\uDC4B",
                fontSize = 180.sp,
                modifier = Modifier
                    .rotate(repeatableAnimation.value)
            )
            Spacer(modifier = Modifier.size(16.dp))
            TesterScreenAnimation(
                show = true,
                delayMillis = 500,
                durationMillis = 500,
            ) {
                Text(
                    text = "Welcome Testers",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
            TesterScreenAnimation(
                show = true,
                delayMillis = 1000,
                durationMillis = 1000,
            ) {
                Text(
                    text = "Let's see which features need to be tested today",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

        }
    }
}


@Composable
fun TesterScreenAnimation(
    modifier: Modifier = Modifier,
    delayMillis: Long = 0L,
    durationMillis: Int = 500,
    show: Boolean = true,
    content: @Composable () -> Unit,
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Start animation after a delay
    LaunchedEffect(show) {
        if(show){
            delay(delayMillis)
            startAnimation = true
        }
    }

    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = (durationMillis * 1.5).toInt()),
        label = "Alpha"
    )

    // Y-axis offset animation (sliding up)
    val offsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 100f,  // Slide up from 100.dp below
        animationSpec = tween(durationMillis = durationMillis),
        label = "OffsetY"
    )

    val offsetPx = with(LocalDensity.current) { offsetY.dp.toPx() }

    // Composable content with the applied animations
    Box(
        modifier = modifier
            .offset { IntOffset(x = 0, y = offsetPx.toInt()) }
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}