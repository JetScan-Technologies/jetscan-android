package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions

import androidx.compose.ui.graphics.Path
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords

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

fun ImageCropCoords.toPath() : Path {
    return Path().apply {
        moveTo(topRight.x.toFloat(), topRight.y.toFloat())
        lineTo(bottomRight.x.toFloat(), bottomRight.y.toFloat())
        lineTo(bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
        lineTo(topLeft.x.toFloat(), topLeft.y.toFloat())
        close()
    }
}
