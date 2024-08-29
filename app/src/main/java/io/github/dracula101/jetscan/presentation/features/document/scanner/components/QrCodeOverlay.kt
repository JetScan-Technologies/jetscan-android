package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke


@Composable
fun QrCodeOverlay(
) {
    val spacing = 30f
    val spacingAnimation = remember {
        Animatable(
            initialValue = spacing,
        )
    }
    LaunchedEffect(Unit) {
        spacingAnimation.animateTo(
            targetValue = spacing * 2,
            animationSpec = TweenSpec(
                durationMillis = 1000,
                delay = 1000
            )
        )
        spacingAnimation.animateTo(
            targetValue = spacing,
            animationSpec = TweenSpec(
                durationMillis = 1000,
                delay = 500
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                val width = size.width
                val height = size.height
                val qrCodeWidth = width * 0.6f
                val qrCodeHeight = width * 0.6f
                val qrCodeX = (width - qrCodeWidth) / 2
                val qrCodeY = (height - qrCodeHeight) / 2
                val segmentLength = 60f

                drawQrCodeOverlay(width, qrCodeY, qrCodeX, qrCodeHeight, qrCodeWidth)
                drawQrCodeCurvedCorner(
                    qrCodeX,
                    qrCodeY,
                    qrCodeWidth,
                    qrCodeHeight,
                    segmentLength
                )
                drawQrCodeCorner(
                    qrCodeX,
                    qrCodeY,
                    qrCodeWidth,
                    qrCodeHeight,
                    segmentLength,
                    spacingAnimation.value
                )
            }
    )
}

private fun ContentDrawScope.drawQrCodeOverlay(
    width: Float,
    qrCodeY: Float,
    qrCodeX: Float,
    qrCodeHeight: Float,
    qrCodeWidth: Float
) {
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, 0f),
        size = Size(width, qrCodeY),
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, qrCodeY),
        size = Size(qrCodeX, qrCodeHeight)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(qrCodeX + qrCodeWidth, qrCodeY),
        size = Size(qrCodeX, qrCodeHeight)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, qrCodeY + qrCodeHeight),
        size = Size(width, qrCodeY)
    )
}

private fun ContentDrawScope.drawQrCodeCurvedCorner(
    qrCodeX: Float,
    qrCodeY: Float,
    qrCodeWidth: Float,
    qrCodeHeight: Float,
    segmentLength: Float
) {
    // make curved corners inside out
    val topLeftCorner = Path().apply {
        moveTo(qrCodeX, qrCodeY + segmentLength)
        lineTo(qrCodeX, qrCodeY)
        lineTo(qrCodeX + segmentLength, qrCodeY)
        cubicTo(
            qrCodeX, qrCodeY,
            qrCodeX, qrCodeY,
            qrCodeX, qrCodeY + segmentLength,
        )
        close()
    }
    drawPath(
        path = topLeftCorner,
        color = Color.Black.copy(alpha = 0.5f)
    )
    val topRightCorner = Path().apply {
        moveTo(qrCodeX + qrCodeWidth - segmentLength, qrCodeY)
        lineTo(qrCodeX + qrCodeWidth, qrCodeY)
        lineTo(qrCodeX + qrCodeWidth, qrCodeY + segmentLength)
        cubicTo(
            qrCodeX + qrCodeWidth, qrCodeY,
            qrCodeX + qrCodeWidth, qrCodeY,
            qrCodeX + qrCodeWidth - segmentLength, qrCodeY,
        )
        close()
    }
    drawPath(
        path = topRightCorner,
        color = Color.Black.copy(alpha = 0.5f)
    )
    val bottomLeftCorner = Path().apply {
        moveTo(qrCodeX, qrCodeY + qrCodeHeight - segmentLength)
        lineTo(qrCodeX, qrCodeY + qrCodeHeight)
        lineTo(qrCodeX + segmentLength, qrCodeY + qrCodeHeight)
        cubicTo(
            qrCodeX, qrCodeY + qrCodeHeight,
            qrCodeX, qrCodeY + qrCodeHeight,
            qrCodeX, qrCodeY + qrCodeHeight - segmentLength,
        )
        close()
    }
    drawPath(
        path = bottomLeftCorner,
        color = Color.Black.copy(alpha = 0.5f)
    )
    val bottomRightCorner = Path().apply {
        moveTo(qrCodeX + qrCodeWidth - segmentLength, qrCodeY + qrCodeHeight)
        lineTo(qrCodeX + qrCodeWidth, qrCodeY + qrCodeHeight)
        lineTo(qrCodeX + qrCodeWidth, qrCodeY + qrCodeHeight - segmentLength)
        cubicTo(
            qrCodeX + qrCodeWidth, qrCodeY + qrCodeHeight,
            qrCodeX + qrCodeWidth, qrCodeY + qrCodeHeight,
            qrCodeX + qrCodeWidth - segmentLength, qrCodeY + qrCodeHeight,
        )
        close()
    }
    drawPath(
        path = bottomRightCorner,
        color = Color.Black.copy(alpha = 0.5f)
    )
}

private fun ContentDrawScope.drawQrCodeCorner(
    qrCodeX: Float,
    qrCodeY: Float,
    qrCodeWidth: Float,
    qrCodeHeight: Float,
    segmentLength: Float,
    spacing: Float
) {
    val topLeftCorner = Path().apply {
        moveTo(qrCodeX - spacing, qrCodeY + spacing + segmentLength)
        lineTo(qrCodeX - spacing, qrCodeY + spacing)
        cubicTo(
            qrCodeX - spacing, qrCodeY + spacing,
            qrCodeX - spacing, qrCodeY - spacing,
            qrCodeX + spacing, qrCodeY - spacing,
        )
        lineTo(qrCodeX + spacing + segmentLength, qrCodeY - spacing)
    }
    drawPath(
        path = topLeftCorner,
        color = Color.White.copy(alpha = 0.7f),
        style = Stroke(
            width = 10f,
            miter = 10f,
            cap = StrokeCap.Butt,
        )
    )
    val topRightCorner = Path().apply {
        moveTo(qrCodeX + qrCodeWidth - spacing - segmentLength, qrCodeY - spacing)
        lineTo(qrCodeX + qrCodeWidth - spacing, qrCodeY - spacing)
        cubicTo(
            qrCodeX + qrCodeWidth - spacing, qrCodeY - spacing,
            qrCodeX + qrCodeWidth + spacing, qrCodeY - spacing,
            qrCodeX + qrCodeWidth + spacing, qrCodeY + spacing,
        )
        lineTo(qrCodeX + qrCodeWidth + spacing, qrCodeY + spacing + segmentLength)
    }
    drawPath(
        path = topRightCorner,
        color = Color.White.copy(alpha = 0.7f),
        style = Stroke(
            width = 10f,
            miter = 10f,
            cap = StrokeCap.Butt,
        )
    )
    val bottomLeftCorner = Path().apply {
        moveTo(qrCodeX - spacing, qrCodeY + qrCodeHeight - spacing - segmentLength)
        lineTo(qrCodeX - spacing, qrCodeY + qrCodeHeight - spacing)
        cubicTo(
            qrCodeX - spacing, qrCodeY + qrCodeHeight - spacing,
            qrCodeX - spacing, qrCodeY + qrCodeHeight + spacing,
            qrCodeX + spacing, qrCodeY + qrCodeHeight + spacing,
        )
        lineTo(qrCodeX + spacing + segmentLength, qrCodeY + qrCodeHeight + spacing)
    }
    drawPath(
        path = bottomLeftCorner,
        color = Color.White.copy(alpha = 0.7f),
        style = Stroke(
            width = 10f,
            miter = 10f,
            cap = StrokeCap.Butt,
        )
    )
    val bottomRightCorner = Path().apply {
        moveTo(qrCodeX + qrCodeWidth - spacing - segmentLength, qrCodeY + qrCodeHeight + spacing)
        lineTo(qrCodeX + qrCodeWidth - spacing, qrCodeY + qrCodeHeight + spacing)
        cubicTo(
            qrCodeX + qrCodeWidth - spacing, qrCodeY + qrCodeHeight + spacing,
            qrCodeX + qrCodeWidth + spacing, qrCodeY + qrCodeHeight + spacing,
            qrCodeX + qrCodeWidth + spacing, qrCodeY + qrCodeHeight - spacing,
        )
        lineTo(qrCodeX + qrCodeWidth + spacing, qrCodeY + qrCodeHeight - spacing - segmentLength)
    }
    drawPath(
        path = bottomRightCorner,
        color = Color.White.copy(alpha = 0.7f),
        style = Stroke(
            width = 10f,
            miter = 10f,
            cap = StrokeCap.Butt,
        )
    )
}