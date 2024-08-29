package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image

import android.graphics.Bitmap
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint

data class RelativeImageCropCoords (
    val topLeft: RelativeCropPoint = RelativeCropPoint(),
    val topRight: RelativeCropPoint = RelativeCropPoint(),
    val bottomLeft: RelativeCropPoint = RelativeCropPoint(),
    val bottomRight: RelativeCropPoint = RelativeCropPoint()
){
    fun toImageCropCoords(bitmap: Bitmap): ImageCropCoords {
        return ImageCropCoords(
            CPoint((bitmap.width * topLeft.xPercent).toDouble(), (bitmap.height * topLeft.yPercent).toDouble()),
            CPoint((bitmap.width * topRight.xPercent).toDouble(), (bitmap.height * topRight.yPercent).toDouble()),
            CPoint((bitmap.width * bottomLeft.xPercent).toDouble(), (bitmap.height * bottomLeft.yPercent).toDouble()),
            CPoint((bitmap.width * bottomRight.xPercent).toDouble(), (bitmap.height * bottomRight.yPercent).toDouble())
        )
    }
}
