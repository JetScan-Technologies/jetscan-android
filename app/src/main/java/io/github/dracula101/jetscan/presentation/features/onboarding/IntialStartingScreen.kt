package io.github.dracula101.jetscan.presentation.features.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialStartingScreen(
    navigateToOnboarding: () -> Unit
) {
    val imageOffsetAnimation = remember { Animatable(0f) }
    val imageFadeAnimation = remember { Animatable(0f) }
    val textOffsetAnimation = remember { Animatable(0f) }
    val textFadeAnimation = remember { Animatable(0f) }
    val subTextOffsetAnimation = remember { Animatable(0f) }
    val subTextFadeAnimation = remember { Animatable(0f) }

    val boxAnimation = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        startAnimation(
            imageOffsetAnimation,
            imageFadeAnimation,
            textOffsetAnimation,
            textFadeAnimation,
            subTextOffsetAnimation,
            subTextFadeAnimation,
            boxAnimation,
            navigateToOnboarding
        )
    }

    JetScanScaffold(
        modifier = Modifier,
    ) { innerPadding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
                    .offset(
                        y = ((1- imageFadeAnimation.value) * 50).dp
                    )
                    .alpha(imageOffsetAnimation.value)
            )
            Text(
                text = "JetScan",
                modifier = Modifier
                    .offset(
                        y = ((1- textFadeAnimation.value) * 50).dp
                    )
                    .alpha(textOffsetAnimation.value),
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "The best way to scan your documents",
                modifier = Modifier
                    .offset(
                        y = ((1- subTextOffsetAnimation.value) * 50).dp
                    )
                    .alpha(subTextFadeAnimation.value),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }

    //circular reveal animation with canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        drawCircle(
            color = primaryColor,
            radius = (size.height * boxAnimation.value)/1.5f,
            center = center
        )
    }
}

private suspend fun <Float, V: AnimationVector> Animatable<Float, V>.animate(
    targetValue: Float,
    durationMillis: Int = 500,
    easing: androidx.compose.animation.core.Easing = LinearOutSlowInEasing,
) {
    animateTo(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = easing
        )
    )
}

private suspend fun startAnimation(
    textOffsetAnimation: Animatable<Float, AnimationVector1D>,
    textFadeAnimation: Animatable<Float, AnimationVector1D>,
    imageOffsetAnimation: Animatable<Float, AnimationVector1D>,
    imageFadeAnimation: Animatable<Float, AnimationVector1D>,
    subTextOffsetAnimation: Animatable<Float, AnimationVector1D>,
    subTextFadeAnimation: Animatable<Float, AnimationVector1D>,
    boxAnimation: Animatable<Float, AnimationVector1D>,
    navigateToOnboarding: () -> Unit
) {
    coroutineScope {
        delay(500)
        launch {
            textOffsetAnimation.animate(1f)
            coroutineScope {
                delay(250)
                launch {
                    imageOffsetAnimation.animate(1f)
                }
                launch {
                    imageFadeAnimation.animate(1f, durationMillis = 800)
                    coroutineScope {
                        launch {
                            subTextOffsetAnimation.animate(1f, durationMillis = 1200)
                            delay(200)
                            boxAnimation.animate(
                                1f,
                                durationMillis = 1000,
                                easing = EaseInExpo
                            )
                            delay(300)
                            launch(Dispatchers.Main){
                                navigateToOnboarding()
                            }
                        }
                        launch {
                            subTextFadeAnimation.animate(1f, durationMillis = 800)
                        }
                    }
                }
            }
        }
        launch {
            textFadeAnimation.animate(1f)
        }
    }
}