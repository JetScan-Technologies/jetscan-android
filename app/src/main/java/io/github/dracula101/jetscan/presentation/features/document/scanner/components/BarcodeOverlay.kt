package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope


@Composable
fun BarCodeOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                val width = size.width
                val height = size.height
                val barcodeWidth = width * 0.6f
                val barcodeHeight = height * 0.2f
                val barcodeX = (width - barcodeWidth) / 2
                val barcodeY = (height - barcodeHeight) / 2
                drawBarCodeOverlay(width, barcodeY, barcodeX, barcodeHeight, barcodeWidth)
                // draw corner lines with angles on White color
                drawBarCodeCorners(barcodeX, barcodeY, barcodeWidth, barcodeHeight)
            }
    )
}

private fun ContentDrawScope.drawBarCodeCorners(
    barcodeX: Float,
    barcodeY: Float,
    barcodeWidth: Float,
    barcodeHeight: Float,
    color: Color = Color.White
) {
    drawLine(
        color = color,
        start = Offset(barcodeX, barcodeY),
        end = Offset(barcodeX + 40f, barcodeY),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX, barcodeY),
        end = Offset(barcodeX, barcodeY + 40f),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX + barcodeWidth - 40f, barcodeY),
        end = Offset(barcodeX + barcodeWidth, barcodeY),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX + barcodeWidth, barcodeY),
        end = Offset(barcodeX + barcodeWidth, barcodeY + 40f),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX, barcodeY + barcodeHeight - 40f),
        end = Offset(barcodeX, barcodeY + barcodeHeight),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX, barcodeY + barcodeHeight),
        end = Offset(barcodeX + 40f, barcodeY + barcodeHeight),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX + barcodeWidth - 40f, barcodeY + barcodeHeight),
        end = Offset(barcodeX + barcodeWidth, barcodeY + barcodeHeight),
        strokeWidth = 4f
    )
    drawLine(
        color = color,
        start = Offset(barcodeX + barcodeWidth, barcodeY + barcodeHeight - 40f),
        end = Offset(barcodeX + barcodeWidth, barcodeY + barcodeHeight),
        strokeWidth = 4f
    )
}

private fun ContentDrawScope.drawBarCodeOverlay(
    width: Float,
    barcodeY: Float,
    barcodeX: Float,
    barcodeHeight: Float,
    barcodeWidth: Float,
    color: Color = Color.Black.copy(alpha = 0.6f)
) {
    drawRect(
        color = color,
        topLeft = Offset(0f, 0f),
        size = Size(width, barcodeY)
    )
    drawRect(
        color = color,
        topLeft = Offset(0f, barcodeY),
        size = Size(barcodeX, barcodeHeight)
    )
    drawRect(
        color = color,
        topLeft = Offset(barcodeX + barcodeWidth, barcodeY),
        size = Size(barcodeX, barcodeHeight)
    )
    drawRect(
        color = color,
        topLeft = Offset(0f, barcodeY + barcodeHeight),
        size = Size(width, barcodeY)
    )
}
