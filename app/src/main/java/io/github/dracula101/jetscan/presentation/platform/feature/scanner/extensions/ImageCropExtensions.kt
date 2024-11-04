package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint

fun ImageCropCoords.scale(scale: Float): ImageCropCoords {
    return ImageCropCoords(
        topLeft = CPoint(
            x = topLeft.x * scale,
            y = topLeft.y * scale
        ),
        topRight = CPoint(
            x = topRight.x * scale,
            y = topRight.y * scale
        ),
        bottomLeft = CPoint(
            x = bottomLeft.x * scale,
            y = bottomLeft.y * scale
        ),
        bottomRight = CPoint(
            x = bottomRight.x * scale,
            y = bottomRight.y * scale
        )
    )
}

fun ImageCropCoords.scale(
    scaleX: Float,
    scaleY: Float
): ImageCropCoords {
    return ImageCropCoords(
        topLeft = CPoint(
            x = topLeft.x * scaleX,
            y = topLeft.y * scaleY
        ),
        topRight = CPoint(
            x = topRight.x * scaleX,
            y = topRight.y * scaleY
        ),
        bottomLeft = CPoint(
            x = bottomLeft.x * scaleX,
            y = bottomLeft.y * scaleY
        ),
        bottomRight = CPoint(
            x = bottomRight.x * scaleX,
            y = bottomRight.y * scaleY
        )
    )
}

data class CornerPointVisibility(
    val topLeft: Boolean = true,
    val topRight: Boolean = true,
    val bottomLeft: Boolean = true,
    val bottomRight: Boolean = true
)

data class HolderVisibility(
    val topCenter: Boolean = true,
    val rightCenter: Boolean = true,
    val bottomCenter: Boolean = true,
    val leftCenter: Boolean = true
)

fun DrawScope.toCropOverlay(
    imageCropCoords: ImageCropCoords,
    primaryColor: Color? = null,
    color: Color = Color.White,
    strokeWidth: Float = 4f,
    cornerRadius: Float = 30f,
    cornerPointVisibility: CornerPointVisibility = CornerPointVisibility(),
    holderVisibility: HolderVisibility = HolderVisibility()
) {

    val outline = Path().apply {
        moveTo(imageCropCoords.topLeft.x.toFloat(), imageCropCoords.topLeft.y.toFloat())
        lineTo(imageCropCoords.topRight.x.toFloat(), imageCropCoords.topRight.y.toFloat())
        lineTo(imageCropCoords.bottomRight.x.toFloat(), imageCropCoords.bottomRight.y.toFloat())
        lineTo(imageCropCoords.bottomLeft.x.toFloat(), imageCropCoords.bottomLeft.y.toFloat())
        close()
    }
    drawPath(
        path = outline,
        color = primaryColor?:color,
        style = Stroke(strokeWidth),
    )

    if (cornerPointVisibility.topLeft) {
        drawCircle(
            color = primaryColor?: color,
            center = Offset(imageCropCoords.topLeft.x.toFloat(), imageCropCoords.topLeft.y.toFloat()),
            style = Stroke(strokeWidth),
            radius = cornerRadius
        )
        drawCircle(
            color = primaryColor?: color,
            center = Offset(imageCropCoords.topLeft.x.toFloat(), imageCropCoords.topLeft.y.toFloat()),
            style = Fill,
            radius = cornerRadius,
            alpha = 0.5f
        )
    }
    if (cornerPointVisibility.topRight) {
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.topRight.x.toFloat(), imageCropCoords.topRight.y.toFloat()),
            style = Stroke(strokeWidth),
            radius = cornerRadius
        )
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.topRight.x.toFloat(), imageCropCoords.topRight.y.toFloat()),
            style = Fill,
            radius = cornerRadius,
            alpha = 0.5f
        )
    }
    if (cornerPointVisibility.bottomLeft) {
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.bottomLeft.x.toFloat(), imageCropCoords.bottomLeft.y.toFloat()),
            style = Stroke(strokeWidth),
            radius = cornerRadius
        )
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.bottomLeft.x.toFloat(), imageCropCoords.bottomLeft.y.toFloat()),
            style = Fill,
            radius = cornerRadius,
            alpha = 0.5f
        )
    }
    if (cornerPointVisibility.bottomRight) {
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.bottomRight.x.toFloat(), imageCropCoords.bottomRight.y.toFloat()),
            style = Stroke(strokeWidth),
            radius = cornerRadius
        )
        drawCircle(
            color = primaryColor?:color,
            center = Offset(imageCropCoords.bottomRight.x.toFloat(), imageCropCoords.bottomRight.y.toFloat()),
            style = Fill,
            radius = cornerRadius,
            alpha = 0.5f
        )
    }
    // add 4 handles (rounded rect - filled) at the middle of each side
    val holderHeight = 30f
    val holderWidth = holderHeight * 3
    val holderCornerRadius = 10f

    val topCenter = Offset(
        x = ((imageCropCoords.topLeft.x + imageCropCoords.topRight.x) / 2f).toFloat(),
        y = ((imageCropCoords.topLeft.y + imageCropCoords.topRight.y) / 2f).toFloat()
    )
    if (holderVisibility.topCenter) {
        withTransform({
            val angle = Math.toDegrees(
                kotlin.math.atan2(
                    (imageCropCoords.topLeft.y - imageCropCoords.topRight.y).toDouble(),
                    (imageCropCoords.topLeft.x - imageCropCoords.topRight.x).toDouble()
                )
            ).toFloat()
            rotate(angle, topCenter)
        }){
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = topCenter.x - holderWidth / 2,
                    y = topCenter.y - holderHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderWidth, holderHeight),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Fill,
            )
            drawRoundRect(
                color = primaryColor?:color,
                topLeft = Offset(
                    x = topCenter.x - holderWidth / 2,
                    y = topCenter.y - holderHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderWidth, holderHeight),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Stroke(strokeWidth),
            )
        }
    }


    val rightCenter = Offset(
        x = ((imageCropCoords.topRight.x + imageCropCoords.bottomRight.x) / 2f).toFloat(),
        y = ((imageCropCoords.topRight.y + imageCropCoords.bottomRight.y) / 2f).toFloat()
    )
    if(holderVisibility.rightCenter){
        withTransform({
            val angle = Math.toDegrees(
                kotlin.math.atan2(
                    (imageCropCoords.topRight.y - imageCropCoords.bottomRight.y).toDouble(),
                    (imageCropCoords.topRight.x - imageCropCoords.bottomRight.x).toDouble()
                )
            ).toFloat()
            rotate(angle + 90f, rightCenter)
        }){
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = rightCenter.x - holderHeight / 2,
                    y = rightCenter.y - holderWidth / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderHeight, holderWidth),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Fill,
            )
            drawRoundRect(
                color = primaryColor?:color,
                topLeft = Offset(
                    x = rightCenter.x - holderHeight / 2,
                    y = rightCenter.y - holderWidth / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderHeight, holderWidth),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Stroke(strokeWidth),
            )
        }
    }

    val bottomCenter = Offset(
        x = ((imageCropCoords.bottomLeft.x + imageCropCoords.bottomRight.x) / 2f).toFloat(),
        y = ((imageCropCoords.bottomLeft.y + imageCropCoords.bottomRight.y) / 2f).toFloat()
    )
    if(holderVisibility.bottomCenter){
        withTransform({
            val angle = Math.toDegrees(
                kotlin.math.atan2(
                    (imageCropCoords.bottomLeft.y - imageCropCoords.bottomRight.y).toDouble(),
                    (imageCropCoords.bottomLeft.x - imageCropCoords.bottomRight.x)
                )
            ).toFloat()
            rotate(angle, bottomCenter)
        }){

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = bottomCenter.x - holderWidth / 2,
                    y = bottomCenter.y - holderHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderWidth, holderHeight),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Fill,
            )
            drawRoundRect(
                color = primaryColor?:color,
                topLeft = Offset(
                    x = bottomCenter.x - holderWidth / 2,
                    y = bottomCenter.y - holderHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderWidth, holderHeight),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Stroke(strokeWidth),
            )
        }
    }

    val leftCenter = Offset(
        x = ((imageCropCoords.topLeft.x + imageCropCoords.bottomLeft.x) / 2f).toFloat(),
        y = ((imageCropCoords.topLeft.y + imageCropCoords.bottomLeft.y) / 2f).toFloat()
    )
    if (holderVisibility.leftCenter){
        withTransform({
            val angle = Math.toDegrees(
                kotlin.math.atan2(
                    (imageCropCoords.topLeft.y - imageCropCoords.bottomLeft.y).toDouble(),
                    (imageCropCoords.topLeft.x - imageCropCoords.bottomLeft.x).toDouble()
                )
            ).toFloat()
            rotate(angle - 90f, leftCenter)
        }) {
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = leftCenter.x - holderHeight / 2,
                    y = leftCenter.y - holderWidth / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderHeight, holderWidth),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Fill,
            )
            drawRoundRect(
                color = primaryColor?:color,
                topLeft = Offset(
                    x = leftCenter.x - holderHeight / 2,
                    y = leftCenter.y - holderWidth / 2
                ),
                size = androidx.compose.ui.geometry.Size(holderHeight, holderWidth),
                cornerRadius = CornerRadius(holderCornerRadius),
                style = Stroke(strokeWidth),
            )
        }
    }


}

@Composable
fun DrawScope.toDocumentOutline(
    imageCropCoords: ImageCropCoords,
    color: Color = Color.White,
    strokeWidth: Float = 4f,
) {
    // animate the drawing of the document outline when the image crop coords change
    val animatableOutline = remember { Animatable(0f) }
    LaunchedEffect(imageCropCoords) {
        animatableOutline.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }
    val outline = Path().apply {
        moveTo(imageCropCoords.topLeft.x.toFloat(), imageCropCoords.topLeft.y.toFloat())
        lineTo(imageCropCoords.topRight.x.toFloat(), imageCropCoords.topRight.y.toFloat())
        lineTo(imageCropCoords.bottomRight.x.toFloat(), imageCropCoords.bottomRight.y.toFloat())
        lineTo(imageCropCoords.bottomLeft.x.toFloat(), imageCropCoords.bottomLeft.y.toFloat())
        close()
    }
    drawPath(
        path = outline,
        color = color,
        style = Stroke(strokeWidth),
        alpha = animatableOutline.value
    )
    drawPath(
        path = outline,
        color = color,
        style = Fill,
        alpha = animatableOutline.value
    )
}