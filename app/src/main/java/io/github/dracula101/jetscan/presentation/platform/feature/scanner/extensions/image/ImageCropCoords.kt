package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image


import android.graphics.Bitmap
import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import kotlinx.parcelize.Parcelize


@Parcelize
data class ImageCropCoords(
    val topLeft: CPoint = CPoint(),
    val topRight: CPoint = CPoint(),
    val bottomLeft: CPoint = CPoint(),
    val bottomRight: CPoint = CPoint()
) : Parcelable {
    fun toList(): List<CPoint> {
        return listOf(topLeft, topRight, bottomLeft, bottomRight)
    }

    fun calculateWidth(): Double {
        return topRight.x - topLeft.x
    }

    fun calculateHeight(): Double {
        return bottomLeft.y - topLeft.y
    }

    fun area(): Double {
        return calculateWidth() * calculateHeight()
    }

    fun isQuadrilateral(): Boolean {
        return !topLeft.arePointsCollinear(topRight, bottomRight) &&
                !topRight.arePointsCollinear(bottomRight, bottomLeft) &&
                !bottomRight.arePointsCollinear(bottomLeft, topLeft) &&
                !bottomLeft.arePointsCollinear(topLeft, topRight)
    }


    fun toJson(): String {
        return "${topLeft.x},${topLeft.y},${topRight.x},${topRight.y},${bottomLeft.x},${bottomLeft.y},${bottomRight.x},${bottomRight.y}"
    }

    fun bound(boundary: Size): ImageCropCoords {
        val topLeft = topLeft.bound(boundary)
        val topRight = topRight.bound(boundary)
        val bottomLeft = bottomLeft.bound(boundary)
        val bottomRight = bottomRight.bound(boundary)
        return ImageCropCoords(topLeft, topRight, bottomLeft, bottomRight)
    }


    companion object {
        fun fromJson(json: String): ImageCropCoords {
            val points = json.split(",")
            return ImageCropCoords(
                CPoint(points[0].toDouble(), points[1].toDouble()),
                CPoint(points[2].toDouble(), points[3].toDouble()),
                CPoint(points[4].toDouble(), points[5].toDouble()),
                CPoint(points[6].toDouble(), points[7].toDouble())
            )
        }

        fun fromBitmap(bitmap: Bitmap): ImageCropCoords {
            val outerSpacingRatio = 0.90
            val aspectRatio = bitmap.width.toDouble() / bitmap.height.toDouble()
            val width = bitmap.width
            val height = bitmap.height
            val outerSpacingHeight = height * outerSpacingRatio
            val outerSpacingWidth = width * outerSpacingRatio
            val innerSpacingHeight = height * (1 - outerSpacingRatio)
            val innerSpacingWidth = width * (1 - outerSpacingRatio)
            return ImageCropCoords(
                CPoint(innerSpacingWidth, innerSpacingHeight),
                CPoint(outerSpacingWidth, innerSpacingHeight),
                CPoint(innerSpacingWidth, outerSpacingHeight),
                CPoint(outerSpacingWidth, outerSpacingHeight)
            )
        }

        fun fromList(it: List<CPoint>): ImageCropCoords {
            return ImageCropCoords(
                topLeft = it[0],
                topRight = it[1],
                bottomLeft = it[2],
                bottomRight = it[3]
            )
        }

        val NONE: ImageCropCoords = ImageCropCoords()
    }
}