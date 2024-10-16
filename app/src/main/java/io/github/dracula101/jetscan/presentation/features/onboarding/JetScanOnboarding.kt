package io.github.dracula101.jetscan.presentation.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.theme.JetScanAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JetScanOnboarding(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    navigateToAuth: () -> Unit,
) {
    val horizontalPager = rememberPagerState { 3 }
    val currentPage = remember{ derivedStateOf { horizontalPager.currentPage } }
    val scope = rememberCoroutineScope()
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(currentPage) {
        viewModel.trySendAction(OnboardingAction.Ui.PageChange(currentPage.value))
    }

    JetScanScaffold(
        alwaysShowBottomBar = true,
        bottomBar = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
            ){
                if(currentPage.value == 2){
                    Row{
                        if(state.value.isLoadingAuthSignIn){
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator(
                                    strokeWidth = 1.5.dp
                                )
                            }
                        }else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .clickable {
                                        viewModel.trySendAction(
                                            OnboardingAction.Ui.ExitOnboarding(
                                                usePasswordlessSignIn = false
                                            )
                                        )
                                        navigateToAuth()
                                    }
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text =  "Login",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .clickable {
                                        viewModel.trySendAction(
                                            OnboardingAction.Ui.ExitOnboarding(
                                                usePasswordlessSignIn = true
                                            )
                                        )
                                    }
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text =  "Skip",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    horizontalPager.animateScrollToPage(horizontalPager.currentPage + 1)
                                }
                            }
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text =  "Next",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    ) { _, windowSize ->
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            HorizontalPager(
                state = horizontalPager,
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) { index ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    OnboardingAnimation(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.65f),
                        delayMillis = 250,
                        show = index == currentPage.value,
                    ) {
                        Image(
                            painter = painterResource(
                                when (index) {
                                    0 -> R.drawable.onboarding_scan
                                    1 -> R.drawable.onboarding_organize
                                    2 -> R.drawable.onboarding_ocr
                                    else -> R.drawable.app_icon
                                }
                            ),
                            modifier = Modifier.then(
                                if(index == 0){
                                    Modifier.scale(0.9f)
                                } else {
                                    Modifier
                                }
                            ),
                            contentDescription = null,
                        )
                    }
                    OnboardingAnimation(
                        modifier = Modifier
                            .padding(bottom = 80.dp),
                        delayMillis = 500,
                        show = index == currentPage.value,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                        ) {
                            Text(
                                text = when (index) {
                                    0 -> "Scan"
                                    1 -> "Organize"
                                    2 -> "OCR"
                                    else -> ""
                                },
                                fontSize = 45.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily(Font(R.font.nunito_sans_semibold))
                            )
                            Text(
                                text = when (index) {
                                    0 -> "Scan your documents with ease and convenience. With JetScan you can easily scan document with multiple image filters."
                                    1 -> "JetScan helps you organize your documents in a better way. You can easily create folders and organize your documents."
                                    2 -> "Use JetScan AI to find text in your documents using OCR. Make your documents searchable and easily findable."
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingAnimation(
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