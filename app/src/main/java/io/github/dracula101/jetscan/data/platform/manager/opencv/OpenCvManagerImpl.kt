package io.github.dracula101.jetscan.data.platform.manager.opencv

import android.graphics.Bitmap
import io.github.dracula101.jetscan.data.platform.utils.LineBundler
import io.github.dracula101.jetscan.data.platform.utils.opencv.applyFilter
import io.github.dracula101.jetscan.data.platform.utils.opencv.bitwiseNot
import io.github.dracula101.jetscan.data.platform.utils.opencv.cannyEdge
import io.github.dracula101.jetscan.data.platform.utils.opencv.closing
import io.github.dracula101.jetscan.data.platform.utils.opencv.colorAdjustment
import io.github.dracula101.jetscan.data.platform.utils.opencv.contourDetection
import io.github.dracula101.jetscan.data.platform.utils.opencv.crop
import io.github.dracula101.jetscan.data.platform.utils.opencv.detectCorners
import io.github.dracula101.jetscan.data.platform.utils.opencv.detectDocument
import io.github.dracula101.jetscan.data.platform.utils.opencv.dilation
import io.github.dracula101.jetscan.data.platform.utils.opencv.erosion
import io.github.dracula101.jetscan.data.platform.utils.opencv.gradient
import io.github.dracula101.jetscan.data.platform.utils.opencv.merge
import io.github.dracula101.jetscan.data.platform.utils.opencv.opening
import io.github.dracula101.jetscan.data.platform.utils.opencv.rotate
import io.github.dracula101.jetscan.data.platform.utils.opencv.saturationChannel
import io.github.dracula101.jetscan.data.platform.utils.opencv.split
import io.github.dracula101.jetscan.data.platform.utils.opencv.toBitmap
import io.github.dracula101.jetscan.data.platform.utils.opencv.toGaussianBlur
import io.github.dracula101.jetscan.data.platform.utils.opencv.toGrayScale
import io.github.dracula101.jetscan.data.platform.utils.opencv.toLabColorSpace
import io.github.dracula101.jetscan.data.platform.utils.opencv.toMat
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Edge
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Graph
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Intersection
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Line
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Node
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class OpenCvManagerImpl : OpenCvManager {

    override fun initialize() {
        OpenCVLoader.initDebug()
    }

    private fun computeLABFilter(image: Mat): Mat {
        val lab = image.toLabColorSpace()
        val channels = lab.split()
        var lChannel = channels[0]
        var aChannel = channels[1]
        val bChannel = channels[2]
        val lKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            Size(L_KERNEL_SIZE, L_KERNEL_SIZE)
        )
        val aKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            Size(A_KERNEL_SIZE, A_KERNEL_SIZE)
        )
        lChannel = lChannel.opening(lKernel)
        aChannel = aChannel.erosion(aKernel, iterations = 3)
        val mergedChannels = ArrayList<Mat>()
        mergedChannels.addAll(listOf(lChannel, aChannel, bChannel))
        val mergedImage = Mat()
        mergedImage.merge(mergedChannels)
        return mergedImage.also{
            releaseResources(listOf(lab, lChannel, aChannel, bChannel))
        }
    }

    private fun computeGradient(image: Mat): Mat {
        val gradientKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            Size(GRADIENT_KERNEL_SIZE, GRADIENT_KERNEL_SIZE)
        )
        val gradient = image.gradient(gradientKernel)
        val closingKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            Size(CLOSING_SIZE, CLOSING_SIZE)
        )
        val closing = gradient.closing(closingKernel)
        return closing.also {
            releaseResources(listOf(gradient))
        }
    }

    override fun detectDocument(
        imageBitmap: Bitmap,
    ): ImageCropCoords? {
        val mat = imageBitmap.toMat()
        val corners = mat.detectCorners()
        if (corners==null && (corners?.size) != 4) return null
        val cornerPoints = corners.map { CPoint(it.x, it.y) }
        val cropCoords = ImageCropCoords(
            topLeft = cornerPoints[0],
            topRight = cornerPoints[1],
            bottomLeft = cornerPoints[3],
            bottomRight = cornerPoints[2]
        )
        return cropCoords
    }

    override fun getLines(imageBitmap: Bitmap): List<Line> {
        val imageMat = imageBitmap.toMat()
        val bitwiseNot = imageMat.bitwiseNot()
        val edges = findEdges(bitwiseNot)
        val lines = detectLines(edges)
        releaseResources(listOf(imageMat, bitwiseNot, edges))
        return lines
    }

    override fun detectSingleDocument(
        imageBitmap: Bitmap,
        onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit
    ): ImageCropCoords? {
        val mat = imageBitmap.toMat()
        val lines = mat.detectDocument()
        val lineBundler = LineBundler(
            minDistanceToMerge = 10.0,
            minAngleToMerge = 30.0,
        )
        val bundledLines = if (lines.size > 4) lineBundler.processLines(lines) else lines
        if (bundledLines.size !in 4..20) return null
        if (bundledLines.size == 4) {
            val intersections = mutableListOf<CPoint>()
            for (i in bundledLines.indices) {
                val line1 = bundledLines[i]
                for (j in i + 1 until bundledLines.size) {
                    val line2 = bundledLines[j]
                    val intersection = line1.intersect(line2)
                    if (intersection != null && intersection.x in 0.0..mat.width().toDouble() && intersection.y in 0.0..mat.height().toDouble()) {
                        intersections.add(intersection)
                    }
                }
            }
            if (intersections.size == 4) {
                return ImageCropCoords(
                    topLeft = intersections[0],
                    topRight = intersections[1],
                    bottomLeft = intersections[2],
                    bottomRight = intersections[3]
                )
            }
        }
        val cornerIntersections = mutableListOf<CPoint>()
        for (i in bundledLines.indices) {
            val line1 = bundledLines[i]
            for (j in i + 1 until bundledLines.size) {
                val line2 = bundledLines[j]
                val intersection = line1.intersect(line2) ?: continue
                if (intersection.x in 0.0..mat.width().toDouble() && intersection.y in 0.0..mat.height().toDouble()) {
                    cornerIntersections.add(intersection)
                }
            }
        }
        if (cornerIntersections.size == 4) {
            return ImageCropCoords(
                topLeft = cornerIntersections[0],
                topRight = cornerIntersections[1],
                bottomLeft = cornerIntersections[2],
                bottomRight = cornerIntersections[3]
            )
        }
        val quadrilateral = findBestQuadrilaterals(
            bundledLines.map { it.toLine() },
            mat.size(),
        )
        if (quadrilateral.isNotEmpty()) {
            return quadrilateral.maxByOrNull { it.area() }
        }
        return null
    }

    private fun findBestQuadrilaterals(lines: List<Line>, size: Size): List<ImageCropCoords> {
        val preprocessedLine = mutableListOf<Line>()
        for (i in lines.indices) {
            val shouldAddLine = lines.filter { addLine(lines[i], it, size) }
            if (shouldAddLine.isNotEmpty()) {
                preprocessedLine.add(lines[i])
            }
        }
        if (preprocessedLine.size !in 4..20) return emptyList()
        val croppedLines = mutableListOf<ImageCropCoords>()
        for (i in 0 until preprocessedLine.size) {
            for (j in i + 1 until preprocessedLine.size) {
                val line1 = preprocessedLine[i]
                val line2 = preprocessedLine[j]
                if (line1.isParallel(line2)) continue
                val intersection = line1.intersection(line2) ?: continue
                if (intersection.x !in 0.0..size.width && intersection.y !in 0.0..size.height) {
                    continue
                }
                for (k in j + 1 until preprocessedLine.size) {
                    val line3 = preprocessedLine[k]
                    if (line2.isParallel(line3)) continue
                    val intersection2 = line2.intersection(line3) ?: continue
                    if (intersection2.x !in 0.0..size.width && intersection2.y !in 0.0..size.height) {
                        continue
                    }
                    for (l in k + 1 until preprocessedLine.size) {
                        val line4 = preprocessedLine[l]
                        if (line3.isParallel(line4)) continue
                        val intersections = getIntersections(
                            listOf(line1, line2, line3, line4),
                            size
                        )
                        if (intersections.size != 4) continue
                        val angle1 = intersections[0].values.first().first.angle(intersections[0].values.first().second)
                        val angle2 = intersections[1].values.first().first.angle(intersections[1].values.first().second)
                        val angle3 = intersections[2].values.first().first.angle(intersections[2].values.first().second)
                        val angle4 = intersections[3].values.first().first.angle(intersections[3].values.first().second)
                        if (angle1 !in 60.0..120.0 || angle2 !in 60.0..120.0 || angle3 !in 60.0..120.0 || angle4 !in 60.0..120.0) {
                            continue
                        }
                        val cropCoords = intersections.map { it.keys.first() }
                        croppedLines.add(
                            ImageCropCoords(
                                topLeft = cropCoords[0],
                                topRight = cropCoords[1],
                                bottomLeft = cropCoords[2],
                                bottomRight = cropCoords[3]
                            )
                        )
                        if (croppedLines.size >= 3) return croppedLines
                    }
                }
            }
        }
        return croppedLines
    }

    private fun getIntersections(lines: List<Line>, size: Size): List<Map<CPoint, Pair<Line, Line>>> {
        val intersections = mutableListOf<Map<CPoint, Pair<Line, Line>>>()
        for (i in lines.indices) {
            val line1 = lines[i]
            for (j in i + 1 until lines.size) {
                val line2 = lines[j]
                val intersection = line1.intersection(line2) ?: continue
                if (intersection.x in 0.0..size.width && intersection.y in 0.0..size.height) {
                    intersections.add(
                        mapOf(
                            intersection to Pair(line1, line2)
                        )
                    )
                }
            }
        }
        return intersections
    }

    private fun addLine(mainLine: Line, otherLine: Line, size: Size) : Boolean {
        if (mainLine.isParallel(otherLine)) {
            return false
        }
        val intersection = mainLine.intersection(otherLine) ?: return false
        if (intersection.x < 0 || intersection.x > size.width || intersection.y < 0 || intersection.y > size.height) {
            return false
        }
        val angle = abs(mainLine.angle(otherLine))
        if (angle !in 70.0..110.0) {
            return false
        }
        return true
    }


    private fun findEdges(
        imageMat: Mat,
        blurRadius: Int = 7,
        cannyLowerLimit: Int = 100,
        cannyUpperLimit: Int = 200
    ): Mat {
        val saturation = imageMat.saturationChannel()
        val blurred = saturation.toGaussianBlur(blurRadius.toFloat())
        val edges = blurred.cannyEdge(cannyLowerLimit.toDouble(), cannyUpperLimit.toDouble())
        return edges.also {
            releaseResources(listOf(saturation, blurred))
        }
    }

    private fun detectLines(
        edges: Mat,
        theta: Double = Math.PI / 90,
        threshold: Int = 65,
        groupSimilarLimit: Int = 30
    ): List<Line> {
        val lines = Mat()
        Imgproc.HoughLines(edges, lines, 1.0, theta, threshold)
        if (lines.empty()) {
            return emptyList()
        }
        val linesArray = mutableListOf<Line>()
        for (i in 0 until lines.rows()) {
            val data = lines.get(i, 0)
            val rho = data[0]
            val angle = data[1]
            val slope = -cos(angle) / sin(angle)
            val yIntercept = rho / sin(angle)
            linesArray.add(Line(slope, yIntercept))
        }
        return groupSimilarLines(linesArray, groupSimilarLimit).also {
            releaseResources(listOf(lines))
        }
    }

    private fun findIntersections(lines: List<Line>, imageSize: Size): List<Intersection> {
        val intersections = mutableListOf<Intersection>()
        for (i in lines.indices) {
            val line1 = lines[i]
            for (j in i + 1 until lines.size) {
                val line2 = lines[j]
                val intersectionPoint = line1.intersection(line2) ?: continue
//                val angle = line1.angleInDegrees(line2)
//                Timber.d("Intersection Point: $intersectionPoint, Angle: $angle")
//                if (angle < MIN_ANGLE_THRESHOLD || angle > MAX_ANGLE_THRESHOLD) continue
                if (intersectionPoint.x < 0 || intersectionPoint.x > imageSize.width || intersectionPoint.y < 0 || intersectionPoint.y > imageSize.height) continue
                intersections.add(Intersection(i + j, Pair(line1, line2), intersectionPoint))
            }
        }
        return intersections.also {
            Timber.d("Intersections (${it.size}")
        }
    }

    private fun buildGraph(intersections: List<Intersection>): Graph {
        val graph = Graph()
        intersections.forEach {
            graph.addNode(
                Node(it.id, it.point)
            )
        }
        intersections.forEach {
            val current = graph.getNode(it.id)
            intersections.forEach { other ->
                if (it != other) {
                    val otherNode = graph.getNode(other.id)
                    if (current != null && otherNode != null) {
                        val edge = Edge(Pair(current, otherNode))
                        graph.addEdge(edge)
                    }
                }
            }
        }
        return graph
    }

    private fun sortPointToCropCoords(points: List<CPoint>): ImageCropCoords {
        val sortedX = points.sortedBy { it.x }
        val sortedY = points.sortedBy { it.y }
        val topLeft = sortedX.first { it.y == sortedY.first().y }
        val bottomRight = sortedX.last { it.y == sortedY.last().y }
        val topRight = sortedX.last { it.y == sortedY.first().y }
        val bottomLeft = sortedX.first { it.y == sortedY.last().y }
        return ImageCropCoords(topLeft, topRight, bottomLeft, bottomRight)
    }

    private fun findQuadrilateral(intersections: List<Intersection>): ImageCropCoords? {
        val cropCoords = mutableListOf<ImageCropCoords>()
        val graph = buildGraph(intersections)
        val nodes = graph.getNodes()
        for (i in nodes.indices) {
            val start = nodes[i]
            for (j in i + 1 until nodes.size) {
                val end = nodes[j]
                val path = graph.depthFirstSearch(start, end)
                if (path.size in 4..8) {
                    val cropCoord = sortPointToCropCoords(path.map { it.point })
                    cropCoords.add(cropCoord)
                }
            }
        }
        return cropCoords.maxByOrNull { it.area() }
    }


    private fun groupSimilarLines(lines: List<Line>, groupSimilarLimit: Int): List<Line> {
        val similarLines = mutableListOf<Line>()
        for (i in lines.indices) {
            val line = lines[i]
            val similarLinesGroup = mutableListOf<Line>()
            similarLinesGroup.add(line)
            for (j in i + 1 until lines.size) {
                val otherLine = lines[j]
                if (isSimilarLine(line, otherLine, groupSimilarLimit)) {
                    similarLinesGroup.add(otherLine)
                }
            }
            if (similarLinesGroup.size > similarLines.size) {
                similarLines.clear()
                similarLines.addAll(similarLinesGroup)
            }
        }
        return similarLines
    }

    private fun isSimilarLine(line1: Line, line2: Line, groupSimilarLimit: Int): Boolean {
        val slopeDiff = Math.abs(line1.slope - line2.slope)
        val interceptDiff = Math.abs(line1.yIntercept - line2.yIntercept)
        return slopeDiff < groupSimilarLimit && interceptDiff < groupSimilarLimit
    }

    override fun cropDocument(imageBitmap: Bitmap, imageCropCoords: ImageCropCoords): Bitmap {
        val mat = imageBitmap.toMat()
        val topLeft = imageCropCoords.topLeft.toPoint()
        val topRight = imageCropCoords.topRight.toPoint()
        val bottomLeft = imageCropCoords.bottomLeft.toPoint()
        val bottomRight = imageCropCoords.bottomRight.toPoint()
        val width = maxOf(getDistance(topLeft, topRight), getDistance(bottomLeft, bottomRight))
        val height = maxOf(getDistance(topLeft, bottomLeft), getDistance(topRight, bottomRight))
        val croppedImage = mat.crop(
            cropPoints = MatOfPoint(
                topLeft,
                topRight,
                bottomLeft,
                bottomRight
            ),
            width = width,
            height = height
        )
        return croppedImage.toBitmap().also {
            releaseResources(listOf(mat))
            releasePointResource(listOf(MatOfPoint(topLeft, topRight, bottomLeft, bottomRight)))
        }
    }

    override fun rotateDocument(imageBitmap: Bitmap, rotationDegrees: Float): Bitmap {
        val mat = imageBitmap.toMat()
        val rotatedMat = mat.rotate(rotationDegrees.toDouble())
        return rotatedMat.toBitmap().also {
            releaseResources(listOf(mat))
        }
    }

    override suspend fun applyFilters(imageBitmap: Bitmap): List<Bitmap> {
        val mat = imageBitmap.toMat()
        val imageFilters = mutableListOf<Bitmap>()
        for (filter in ImageFilter.entries) {
            val filteredImage = mat.applyFilter(filter)
            imageFilters.add(filteredImage.toBitmap())
        }
        releaseResources(listOf(mat))
        return imageFilters
    }

    override fun applyFilter(imageBitmap: Bitmap, filter: ImageFilter): Bitmap {
        val mat = imageBitmap.toMat()
        val filteredImage = mat.applyFilter(filter)
        return filteredImage.toBitmap().also {
            releaseResources(listOf(mat))
        }
    }

    override fun applyColorAdjustment(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Bitmap {
        val mat = bitmap.toMat()
        val colorAdjustedMat = mat.colorAdjustment(brightness, contrast, saturation)
        return colorAdjustedMat.toBitmap().also {
            releaseResources(listOf(mat))
        }
    }

    private fun getDistance(p1: Point, p2: Point): Double {
        return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
    }

    private fun releaseResources(mats: List<Mat>) {
        mats.forEach { it.release() }
    }

    private fun releasePointResource(points: List<MatOfPoint>) {
        points.forEach { it.release() }
    }

    companion object {
        private const val BLUR_KERNEL_SIZE = 3f
        private const val L_KERNEL_SIZE = 13.0
        private const val A_KERNEL_SIZE = 5.0
        private const val GRADIENT_KERNEL_SIZE = 3.0
        private const val WEIGHTAGE_VALUE = 0.8
        private const val CLOSING_SIZE = 7.0
        private const val DILATION_SIZE = 8.0
        private const val EROSION_SIZE = 3.0

        // private const val CANNY_LOWER_THRESHOLD = 60.0
        private const val CANNY_LOWER_THRESHOLD = 150.0
        private const val CANNY_UPPER_THRESHOLD = 400.0
        private const val NUM_TOP_CONTOURS = 10
        private const val MIN_ANGLE_THRESHOLD = 45.0
        private const val MAX_ANGLE_THRESHOLD = 150.0
    }

}