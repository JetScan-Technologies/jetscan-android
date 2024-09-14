package io.github.dracula101.pdf.util

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.launch

//
//fun Modifier.drawHorizontalScrollbar(
//    state: ScrollState,
//    reverseScrolling: Boolean = false
//): Modifier = drawScrollbar(state, Orientation.Horizontal, reverseScrolling)
//
//fun Modifier.drawVerticalScrollbar(
//    state: ScrollState,
//    reverseScrolling: Boolean = false
//): Modifier = drawScrollbar(state, Orientation.Vertical, reverseScrolling)
//
//private fun Modifier.drawScrollbar(
//    state: ScrollState,
//    orientation: Orientation,
//    reverseScrolling: Boolean
//): Modifier = drawScrollbar(
//    orientation, reverseScrolling
//) { reverseDirection, atEnd, color, alpha ->
//    if (state.maxValue > 0) {
//        val canvasSize = if (orientation == Orientation.Horizontal) size.width else size.height
//        val totalSize = canvasSize + state.maxValue
//        val thumbSize = canvasSize / totalSize * canvasSize
//        val startOffset = state.value / totalSize * canvasSize
//        drawScrollbar(
//            orientation, reverseDirection, atEnd, color, alpha, startOffset
//        )
//    }
//}
//
//fun Modifier.drawHorizontalScrollbar(
//    state: LazyListState,
//    reverseScrolling: Boolean = false
//): Modifier = drawScrollbar(state, Orientation.Horizontal, reverseScrolling)
//
//fun Modifier.drawVerticalScrollbar(
//    state: LazyListState,
//    reverseScrolling: Boolean = false
//): Modifier = drawScrollbar(state, Orientation.Vertical, reverseScrolling)
//
//private fun Modifier.drawScrollbar(
//    state: LazyListState,
//    orientation: Orientation,
//    reverseScrolling: Boolean
//): Modifier = drawAnimatedScrollbar(
//    state, orientation, reverseScrolling
//) { reverseDirection, atEnd, color, alpha ->
//    val layoutInfo = state.layoutInfo
//    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
//    val items = layoutInfo.visibleItemsInfo
//    val itemsSize = items.fastSumBy { it.size }
//    if (items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize) {
//        val estimatedItemSize = if (items.isEmpty()) 0f else itemsSize.toFloat() / items.size
//        val totalSize = estimatedItemSize * layoutInfo.totalItemsCount
//        val canvasSize = if (orientation == Orientation.Horizontal) size.width else size.height
//        val thumbSize = viewportSize / totalSize * canvasSize
//        val startOffset = if (items.isEmpty()) 0f else items.first().run {
//            (estimatedItemSize * index - offset) / totalSize * canvasSize
//        }
//        drawScrollbar(
//            orientation, reverseDirection, atEnd, color, { alpha }, startOffset
//        )
//    }
//}
//
//fun Modifier.drawVerticalScrollbar(
//    state: LazyGridState,
//    spanCount: Int,
//    reverseScrolling: Boolean = false
//): Modifier = drawScrollbar(
//    Orientation.Vertical, reverseScrolling,
//) { reverseDirection, atEnd, color, alpha ->
//    val layoutInfo = state.layoutInfo
//    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
//    val items = layoutInfo.visibleItemsInfo
//    val rowCount = (items.size + spanCount - 1) / spanCount
//    var itemsSize = 0
//    for (i in 0 until rowCount) {
//        itemsSize += items[i * spanCount].size.height
//    }
//    if (items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize) {
//        val estimatedItemSize = if (rowCount == 0) 0f else itemsSize.toFloat() / rowCount
//        val totalRow = (layoutInfo.totalItemsCount + spanCount - 1) / spanCount
//        val totalSize = estimatedItemSize * totalRow
//        val canvasSize = size.height
//        val thumbSize = viewportSize / totalSize * canvasSize
//        val startOffset = if (rowCount == 0) 0f else items.first().run {
//            val rowIndex = index / spanCount
//            (estimatedItemSize * rowIndex - offset.y) / totalSize * canvasSize
//        }
//        drawScrollbar(
//            Orientation.Vertical, reverseDirection, atEnd, color, alpha, startOffset
//        )
//    }
//}
//
//private fun DrawScope.drawScrollbar(
//    orientation: Orientation,
//    reverseDirection: Boolean,
//    atEnd: Boolean,
//    color: Color,
//    alpha: () -> Float,
//    startOffset: Float
//) {
//    val thicknessPx = 60.dp.value
//    val thumbSize = 60.dp.value
//    val topLeft = if (orientation == Orientation.Horizontal) {
//        Offset(
//            if (reverseDirection)  size.width - startOffset - thumbSize else startOffset,
//            if (atEnd) size.height - thicknessPx else 0f
//        )
//    } else {
//        Offset(
//            if (atEnd) size.width - thicknessPx else 0f,
//            if (reverseDirection) size.height - startOffset - thumbSize else startOffset
//        )
//    }
//    val size = if (orientation == Orientation.Horizontal) {
//        Size(thumbSize, thicknessPx)
//    } else {
//        Size(thicknessPx, thumbSize)
//    }
//    drawRoundRect(
//        topLeft = topLeft,
//        size = size,
//        color = color,
//        cornerRadius = CornerRadius(20f, 10f),
//    )
//}
//
//private fun Modifier.drawScrollbar(
//    orientation: Orientation,
//    reverseScrolling: Boolean,
//    onDraw: DrawScope.(
//        reverseDirection: Boolean,
//        atEnd: Boolean,
//        color: Color,
//        alpha: () -> Float
//    ) -> Unit
//): Modifier = composed {
//    val scrolled = remember {
//        MutableSharedFlow<Unit>(
//            extraBufferCapacity = 1,
//            onBufferOverflow = BufferOverflow.DROP_OLDEST
//        )
//    }
//    val nestedScrollConnection = remember(orientation, scrolled) {
//        object : NestedScrollConnection {
//            override fun onPostScroll(
//                consumed: Offset,
//                available: Offset,
//                source: NestedScrollSource
//            ): Offset {
//                val delta = if (orientation == Orientation.Horizontal) consumed.x else consumed.y
//                if (delta != 0f) scrolled.tryEmit(Unit)
//                return Offset.Zero
//            }
//        }
//    }
//
//    val alpha = remember { Animatable(0f) }
//    LaunchedEffect(scrolled, alpha) {
//        scrolled.collectLatest {
//            alpha.snapTo(1f)
//            delay(ViewConfiguration.getScrollDefaultDelay().toLong())
//            alpha.animateTo(0f, animationSpec = FadeOutAnimationSpec)
//        }
//    }
//
//    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
//    val reverseDirection = if (orientation == Orientation.Horizontal) {
//        if (isLtr) reverseScrolling else !reverseScrolling
//    } else reverseScrolling
//    val atEnd = if (orientation == Orientation.Vertical) isLtr else true
//
//    val color = BarColor
//
//    Modifier
//        .nestedScroll(nestedScrollConnection)
//        .drawWithContent {
//            drawContent()
//            onDraw(reverseDirection, atEnd, color, alpha::value)
//        }
//}
//
//
//private fun Modifier.drawAnimatedScrollbar(
//    lazyListState: LazyListState,
//    orientation: Orientation,
//    reverseScrolling: Boolean,
//    onDraw: DrawScope.(
//        reverseDirection: Boolean,
//        atEnd: Boolean,
//        color: Color,
//        alpha: Float
//    ) -> Unit
//): Modifier = composed {
//    val scrolled = remember {
//        MutableSharedFlow<Unit>(
//            extraBufferCapacity = 1,
//            onBufferOverflow = BufferOverflow.DROP_OLDEST
//        )
//    }
//    val nestedScrollConnection = remember(orientation, scrolled) {
//        object : NestedScrollConnection {
//            override fun onPostScroll(
//                consumed: Offset,
//                available: Offset,
//                source: NestedScrollSource
//            ): Offset {
//                val delta = if (orientation == Orientation.Horizontal) consumed.x else consumed.y
//                if (delta != 0f) scrolled.tryEmit(Unit)
//                return Offset.Zero
//            }
//        }
//    }
//
//    val alpha = animateFloatAsState(
//        if(lazyListState.isScrollInProgress) 1f else 0f,
//        label = "Fade Animation Scrollbar"
//    )
//
//    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
//    val reverseDirection = if (orientation == Orientation.Horizontal) {
//        if (isLtr) reverseScrolling else !reverseScrolling
//    } else reverseScrolling
//    val atEnd = if (orientation == Orientation.Vertical) isLtr else true
//
//    val color = BarColor
//
//    Modifier
//        .nestedScroll(nestedScrollConnection)
//        .drawWithContent {
//            drawContent()
//            onDraw(reverseDirection, atEnd, color, alpha.value)
//        }
//}
//
//private val BarColor: Color
//    get() = Color.White
//
//private val FadeOutAnimationSpec =
//    tween<Float>(durationMillis = ViewConfiguration.getScrollBarFadeDuration())


@Composable
fun VerticalScrollBar(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    constraints: Constraints,
    scrollMarker: @Composable (currentIndex: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val yOffset = remember { mutableStateOf(30f) }
    val currentItem = remember { mutableStateOf(0) }
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }.collect { layoutInfo ->
            val topPadding = 140f
            val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset - topPadding
            val items = layoutInfo.visibleItemsInfo
            val itemsSize = items.fastSumBy { it.size }
            if (items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize) {
                val estimatedItemSize = if (items.isEmpty()) 0f else itemsSize.toFloat() / items.size
                val totalSize = estimatedItemSize * layoutInfo.totalItemsCount
                val canvasSize = constraints.maxHeight.toFloat()
                val startOffset =  items.first().run {
                    (estimatedItemSize * index - offset) / totalSize * canvasSize + topPadding
                }
                yOffset.value = startOffset
                currentItem.value = items.first().index
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = yOffset.value
                }
        ) {
            scrollMarker(currentItem.value)
        }
    }
}
