package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.sqrt

@Parcelize
data class CPoint (
    val x: Double = 0.0,
    val y: Double = 0.0
) : Parcelable {

    fun distance(point: CPoint) : Float {
        return sqrt(((point.x - x) * (point.x - x) + (point.y - y) * (point.y - y)).toDouble()).toFloat()
    }

    fun distance(offset: Offset): Float {
        return sqrt(((offset.x - x) * (offset.x - x) + (offset.y - y) * (offset.y - y)).toDouble()).toFloat()
    }

    fun arePointsCollinear(point1: CPoint, point2: CPoint) : Boolean {
        val x1 = point1.x.toFloat()
        val y1 = point1.y.toFloat()
        val x2 = point2.x.toFloat()
        val y2 = point2.y.toFloat()
        val x3 = x.toFloat()
        val y3 = y.toFloat()
        val isCollinear =  abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)).toDouble()) < 0.0001
        return isCollinear
    }

    fun toOffset(): Offset {
        return Offset(x.toFloat(), y.toFloat())
    }


    fun minus(x: Float, y: Float): CPoint {
        return CPoint(this.x - x, this.y - y)
    }

    fun minus(value: Float): CPoint {
        return CPoint(this.x - value, this.y - value)
    }

    fun distanceTo(position: Offset) : Float {
        return sqrt(((position.x - x) * (position.x - x) + (position.y - y) * (position.y - y))).toFloat()
    }

    fun bound(boundary: Size): CPoint {
        return CPoint(
            x = x.coerceIn(0.0, boundary.width.toDouble()),
            y = y.coerceIn(0.0, boundary.height.toDouble())
        )
    }
}

