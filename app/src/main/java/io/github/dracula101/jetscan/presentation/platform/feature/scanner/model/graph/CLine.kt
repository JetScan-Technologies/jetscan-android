package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph

data class CLine(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    val length: Double
        get() = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

    fun toArray(): DoubleArray {
        return doubleArrayOf(x1, y1, x2, y2)
    }

    fun intersect(line: CLine, pointLimit: Double = 20.0) : CPoint? {
        val x1 = x1
        val y1 = y1
        val x2 = x2
        val y2 = y2
        val x3 = line.x1
        val y3 = line.y1
        val x4 = line.x2
        val y4 = line.y2

        val det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (det == 0.0) {
            return null
        }

        val x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det
        val y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det

        if (x < minOf(x1, x2) - pointLimit || x > maxOf(x1, x2) + pointLimit) {
            return null
        }
        if (x < minOf(x3, x4) - pointLimit || x > maxOf(x3, x4) + pointLimit) {
            return null
        }
        if (y < minOf(y1, y2) - pointLimit || y > maxOf(y1, y2) + pointLimit) {
            return null
        }
        if (y < minOf(y3, y4) - pointLimit || y > maxOf(y3, y4) + pointLimit) {
            return null
        }

        return CPoint(x, y)
    }

    fun toLine() : Line {
        val slope = (y2 - y1) / (x2 - x1)
        val yIntercept = y1 - slope * x1
        return Line(slope, yIntercept)
    }

    override fun toString(): String {
        return "($x1, $y1) -> ($x2, $y2)"
    }


}
