package io.github.dracula101.pdf.ui

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.TransformScope
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.panBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberPdfMarker(): PdfMarker =
    remember { PdfMarker() }

class PdfMarker

@Composable
fun rememberPdfMarkerTransformState(
    lazyListState: LazyListState,
    scope: CoroutineScope = rememberCoroutineScope(),
): PdfMarkerTransformState {
    return remember {
        PdfMarkerTransformState(
            scope = scope,
            verticalScrollState = lazyListState,
        )
    }
}

class PdfMarkerTransformState(
    val scope: CoroutineScope,
    val verticalScrollState: LazyListState,
) : TransformableState {

    var totalHeight = 0f

    var currentY = mutableStateOf(0f)
        private set

    private val transformableState = TransformableState {_, panChange, _ ->
        val newCurrentY = currentY.value + panChange.y
        currentY.value = newCurrentY.coerceIn(0f, totalHeight)
        scope.launch {
            verticalScrollState.scrollToItem(newCurrentY.toInt())
        }
    }

    override val isTransformInProgress: Boolean
        get() = transformableState.isTransformInProgress

    override suspend fun transform(
        transformPriority: MutatePriority,
        block: suspend TransformScope.() -> Unit
    ) {
        transformableState.transform(transformPriority, block)
    }

    suspend fun scrollToItem(index: Int) {
        verticalScrollState.scrollToItem(index)
    }

    suspend fun panBy(panChange: Offset) {
        transformableState.panBy(panChange)
    }

    fun GraphicsLayerScope.setGraphicsLayer() {
        translationY = currentY.value
    }

}

fun Modifier.pdfMarkerTransformable(
    state: PdfMarkerTransformState,
    height : Float,
): Modifier = composed {
    state.totalHeight = height

    val scope = rememberCoroutineScope()

    val verticalScrollState = remember {
        ScrollableState { change ->
            scope.launch {
                state.transform(MutatePriority.PreventUserInput) {
                    state.panBy(Offset(0f, -change))
                }
            }
            change
        }
    }

    this
        .scrollable(
            orientation = Orientation.Vertical,
            state = verticalScrollState,
        )
}

fun Modifier.graphicsLayerWithPdfMarkerTransform(
    state: PdfMarkerTransformState,
): Modifier = composed {
    this
        .graphicsLayer {
            with(state) {
                setGraphicsLayer()
            }
        }
}