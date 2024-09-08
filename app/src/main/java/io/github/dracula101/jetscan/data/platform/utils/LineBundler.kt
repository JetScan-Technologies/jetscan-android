package io.github.dracula101.jetscan.data.platform.utils

import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CLine
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class LineBundler(
    val minDistanceToMerge: Double = 10.0,
    val minAngleToMerge: Double = 30.0
) {

    private fun getOrientation(line: CLine): Double {
        val angle = atan2(line.y2 - line.y1, line.x2 - line.x1)
        return Math.toDegrees(angle)
    }

    private fun checker(
        lineNew: CLine,
        groups: MutableList<MutableList<CLine>>,
        minDistanceToMerge: Double,
        minAngleToMerge: Double
    ): Boolean {
        for (group in groups) {
            for (lineOld in group) {
                if (getDistance(lineOld, lineNew) < minDistanceToMerge) {
                    val orientationNew = getOrientation(lineNew)
                    val orientationOld = getOrientation(lineOld)
                    if (abs(orientationNew - orientationOld) < minAngleToMerge) {
                        group.add(lineNew)
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun distancePointLine(point: CPoint, line: CLine): Double {
        val px = point.x
        val py = point.y
        val x1 = line.x1
        val y1 = line.y1
        val x2 = line.x2
        val y2 = line.y2


        fun lineMagnitude(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
        }

        val lineMag = lineMagnitude(x1, y1, x2, y2)
        if (lineMag < 0.00000001) return 9999.0

        val u1 = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1))
        val u = u1 / (lineMag * lineMag)

        return if (u < 0.00001 || u > 1) {
            min(lineMagnitude(px, py, x1, y1), lineMagnitude(px, py, x2, y2))
        } else {
            val ix = x1 + u * (x2 - x1)
            val iy = y1 + u * (y2 - y1)
            lineMagnitude(px, py, ix, iy)
        }
    }

    private fun getDistance(aLine: CLine, bLine: CLine): Double {
        val distance1 = distancePointLine(CPoint(bLine.x1, bLine.y1), aLine)
        val distance2 = distancePointLine(CPoint(bLine.x2, bLine.y2), aLine)
        val distance3 = distancePointLine(CPoint(aLine.x1, aLine.y1), bLine)
        val distance4 = distancePointLine(CPoint(aLine.x2, aLine.y2), bLine)
        return min(min(distance1, distance2), min(distance3, distance4))
    }

    private fun mergeLinesPipeline(lines: List<CLine>): MutableList<MutableList<CLine>> {
        val groups = mutableListOf<MutableList<CLine>>()
        groups.add(mutableListOf(lines[0]))

        for (lineNew in lines.drop(1)) {
            if (checker(lineNew, groups, minDistanceToMerge, minAngleToMerge)) {
                groups.add(mutableListOf(lineNew))
            }
        }

        return groups
    }

    private fun mergeLinesSegments(lines: List<CLine>): List<CPoint> {
        val orientation = getOrientation(lines[0])

        if (lines.size == 1) {
            return listOf(CPoint(lines[0].x1, lines[0].y1), CPoint(lines[0].x2, lines[0].y2))
        }

        val points = mutableListOf<CPoint>()
        for (line in lines) {
            points.add(CPoint(line.x1, line.y1))
            points.add(CPoint(line.x2, line.y2))
        }

        points.sortWith { p1, p2 ->
            if (45.0 < orientation && orientation < 135.0) {
                p1.y.compareTo(p2.y)
            } else {
                p1.x.compareTo(p2.x)
            }
        }

        return listOf(points.first(), points.last())
    }

    fun processLines(lines: List<CLine>): List<CLine> {
        val linesX = mutableListOf<CLine>()
        val linesY = mutableListOf<CLine>()

        for (line in lines) {
            val orientation = getOrientation(line)
            if (45.0 < orientation && orientation < 135.0) {
                linesY.add(line)
            } else {
                linesX.add(line)
            }
        }

        linesY.sortBy { it.y1 }
        linesX.sortBy { it.x1 }

        val mergedLinesAll = mutableListOf<CLine>()

        for (lineSet in listOf(linesX, linesY)) {
            if (lineSet.isNotEmpty()) {
                val groups = mergeLinesPipeline(lineSet)
                val mergedLines = groups.map { mergeLinesSegments(it) }
                mergedLinesAll.addAll(mergedLines.map { CLine(it[0].x, it[0].y, it[1].x, it[1].y) })
            }
        }
        return mergedLinesAll
    }
}