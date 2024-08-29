package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph

import kotlin.math.atan

data class Line(
    val slope: Double,
    val yIntercept: Double
) {
    fun x(y: Double): Double {
        return (y - yIntercept) / slope
    }

    fun y(x: Double): Double {
        return slope * x + yIntercept
    }

    fun intersection(line: Line): CPoint {
        val x = (line.yIntercept - yIntercept) / (slope - line.slope)
        val y = slope * x + yIntercept
        return CPoint(x, y)
    }

    fun isParallel(line: Line): Boolean {
        return slope == line.slope
    }

    fun isPerpendicular(line: Line): Boolean {
        return slope * line.slope == -1.0
    }

    fun angle(line: Line): Double {
        return Math.toDegrees(atan((line.slope - slope) / (1 + line.slope * slope)))
    }

    fun onLine(point: CPoint): Boolean {
        return point.y == slope * point.x + yIntercept
    }
}

