package io.github.dracula101.pdf.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import io.github.dracula101.pdf.util.VerticalScrollBar

@Composable
fun PdfReader(
    file: File?,
    modifier: Modifier = Modifier,
    loader: PdfLoader = rememberPdfLoader(file),
    lazyListState: LazyListState = rememberLazyListState(),
    pdfTransformState: PdfTransformState = rememberPdfTransformState(file, lazyListState),
    initContent: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {
        CircularProgressIndicator()
    },
    errorContent: @Composable (PdfLoader.State.Error) -> Unit = { state ->
        Text(text = state.throwable.message.orEmpty())
    },
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 32.dp),
    pageBackground: Color = Color.Transparent,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = loader.state) {
            is PdfLoader.State.Init -> initContent()
            is PdfLoader.State.Loading -> loadingContent()
            is PdfLoader.State.Error -> errorContent(state)
            is PdfLoader.State.Success -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val maxPageWidth = remember { loader.pdfPages.maxOf { it.width } }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayerWithPdfTransform(pdfTransformState),
                        contentPadding = contentPadding,
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        items(loader.pdfPages) { pageInfo ->
                            val width = maxWidth * (pageInfo.width.toFloat() / maxPageWidth)
                            val height = width * (pageInfo.height.toFloat() / pageInfo.width)

                            val widthPx = width.roundToPx()
                            val heightPx = height.roundToPx()

                            Box(
                                modifier = Modifier.size(maxWidth, height),
                                contentAlignment = Alignment.Center,
                            ) {
                                when (val pageContent = pageInfo.state.collectAsState().value) {
                                    is PdfLoader.PageContent.Empty -> {
                                        Box(
                                            modifier = Modifier
                                                .size(width, height)
                                                .background(pageBackground)
                                        )
                                    }

                                    is PdfLoader.PageContent.Content -> {
                                        Image(
                                            modifier = Modifier
                                                .size(width, height)
                                                .background(Color.White),
                                            painter = BitmapPainter(pageContent.bitmap),
                                            contentDescription = pageContent.contentDescription
                                        )
                                    }
                                }

                                LaunchedEffect(pdfTransformState.scale) {
                                    pageInfo.render(widthPx, heightPx, pdfTransformState.scale)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .pdfTransformable(pdfTransformState, constraints)
                            .fillMaxSize()
                    )
                    VerticalScrollBar(
                        lazyListState = lazyListState,
                        constraints = constraints,
                        scrollMarker = { currentIndex ->
                            MarkerComposable(currentPage = currentIndex, totalPages = loader.pdfPages.size)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkerComposable(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 40.dp,
        bottomStart = 40.dp
    ),
    yOffset : Float = 200f,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .boxShadow(
                    color = Color.Black.copy(alpha = 0.2f),
                    offsetX = (4).dp,
                    offsetY = (4).dp,
                    blurRadius = 8.dp,
                )
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(top = 2.dp, bottom = 2.dp, start = 8.dp, end = 40.dp)
        ) {
            Text(
                text = "${currentPage + 1} / $totalPages",
                color = Color.Black,
                fontSize = 12.sp,
            )
        }
        Box(
            modifier = Modifier
                .boxShadow(
                    color = Color.Black.copy(alpha = 0.2f),
                    offsetX = (4).dp,
                    offsetY = (4).dp,
                    blurRadius = 8.dp,
                )
                .height(45.dp)
                .width(35.dp)
                .clip(shape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Marker",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Marker",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
            }

        }

    }
}

@Composable
private fun Dp.roundToPx() = with(LocalDensity.current) { roundToPx() }


fun Modifier.boxShadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter = (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }
            frameworkPaint.color = color.toArgb()

            val leftPixel = offsetX.toPx()
            val topPixel = offsetY.toPx()
            val rightPixel = size.width + topPixel
            val bottomPixel = size.height + leftPixel

            canvas.drawRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                paint = paint,
            )
        }
    }
)