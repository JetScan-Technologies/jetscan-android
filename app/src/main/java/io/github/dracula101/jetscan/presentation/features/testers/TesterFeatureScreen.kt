package io.github.dracula101.jetscan.presentation.features.testers

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TesterFeatureScreen(
    onBackNavigation: () -> Unit
) {
    val pagerState = rememberPagerState { 4 }
    val verticalScrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    val scope = rememberCoroutineScope()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    JetScanScaffold(
        alwaysShowBottomBar = true,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleButton(
                    imageVector = Icons.Rounded.ArrowBack,
                    onClick = {
                        if (pagerState.currentPage != 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage != 0
                )
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){

                    Canvas(modifier = Modifier.width(width = 50.dp)) {
                        val spacing = 5.dp.toPx()
                        val dotWidth = 10.dp.toPx()
                        val dotHeight = 10.dp.toPx()

                        val activeDotWidth = 20.dp.toPx()
                        var x = 0f
                        val y = center.y

                        repeat(4) { i ->
                            val posOffset = pagerState.pageOffset
                            val dotOffset = posOffset % 1
                            val current = posOffset.toInt()

                            val factor = (dotOffset * (activeDotWidth - dotWidth))

                            val calculatedWidth = when {
                                i == current -> activeDotWidth - factor
                                i - 1 == current || (i == 0 && posOffset > 3) -> dotWidth + factor
                                else -> dotWidth
                            }

                            drawIndicator(
                                x = x,
                                y = y,
                                width = calculatedWidth,
                                height = dotHeight,
                                radius = CornerRadius(50f),
                                color = onSurfaceColor,
                                i == pagerState.currentPage
                            )
                            x += calculatedWidth + spacing
                        }
                    }
                }
                CircleButton(
                    imageVector = Icons.Rounded.ArrowForward,
                    onClick = {
                        if (pagerState.currentPage != 3) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onBackNavigation()
                        }
                    }
                )

            }
        }
    ) { padding, windowSize ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) { page ->
                Column (
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ){
                    Text(
                        when (page) {
                            0 -> "Scan Docs"
                            1 -> "Edit and PDF"
                            2 -> "Home Tabs"
                            3 -> "App Settings"
                            else -> ""
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(Font(R.font.nunito_sans_bold)),
                    )
                    Text(
                        when (page) {
                            0 -> "Scan Documents by clicking the scan button, and save the pdf via all the cropping tools"
                            1 -> "After scanning the document, you can edit the document(some features are pending) and see the pdf"
                            2 -> "Explore the home tabs and make some folder to check its functionality"
                            3 -> "Change the app settings and see the changes, for eg: the theme of the app, settings for document"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.size(24.dp))
                    Image(
                        painter = rememberAsyncImagePainter(
                            when (page) {
                                0 -> R.drawable.scan_camera_testers
                                1 -> R.drawable.pdf_route_testers
                                2 -> R.drawable.home_tab_testers
                                3 -> R.drawable.app_settings_testers
                                else -> R.drawable.scan_camera_testers
                            },
                            imageLoader = imageLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.95f)
                    )

                }
            }
        }

    }
}

private fun DrawScope.drawIndicator(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    radius: CornerRadius,
    color: Color,
    isSelected: Boolean = true
) {
    val rect = RoundRect(
        x,
        y - height / 2,
        x + width,
        y + height / 2,
        radius
    )
    val path = Path().apply { addRoundRect(rect) }
    drawPath(path = path, color = color.copy(alpha = if (isSelected) 1f else 0.5f))
}

// To get scroll offset
@OptIn(ExperimentalFoundationApi::class)
val PagerState.pageOffset: Float
    get() = this.currentPage + this.currentPageOffsetFraction


// To get scrolled offset from snap position
@OptIn(ExperimentalFoundationApi::class)
fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}