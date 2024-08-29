package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions

import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import org.opencv.core.Point


fun Point.toCPoint(): CPoint {
    return CPoint(x.toDouble(), y.toDouble())
}

fun CPoint.toPoint() : Point {
    return Point(x, y)
}