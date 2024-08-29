package io.github.dracula101.jetscan.data.platform.manager.opencv

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Size
import io.github.dracula101.jetscan.data.platform.utils.opencv.applyFilter
import io.github.dracula101.jetscan.data.platform.utils.opencv.bitwiseNot
import io.github.dracula101.jetscan.data.platform.utils.opencv.cannyEdge
import io.github.dracula101.jetscan.data.platform.utils.opencv.closing
import io.github.dracula101.jetscan.data.platform.utils.opencv.colorAdjustment
import io.github.dracula101.jetscan.data.platform.utils.opencv.contourDetection
import io.github.dracula101.jetscan.data.platform.utils.opencv.convexHull
import io.github.dracula101.jetscan.data.platform.utils.opencv.crop
import io.github.dracula101.jetscan.data.platform.utils.opencv.dilation
import io.github.dracula101.jetscan.data.platform.utils.opencv.erosion
import io.github.dracula101.jetscan.data.platform.utils.opencv.houghTransform
import io.github.dracula101.jetscan.data.platform.utils.opencv.merge
import io.github.dracula101.jetscan.data.platform.utils.opencv.opening
import io.github.dracula101.jetscan.data.platform.utils.opencv.rotate
import io.github.dracula101.jetscan.data.platform.utils.opencv.split
import io.github.dracula101.jetscan.data.platform.utils.opencv.toBitmap
import io.github.dracula101.jetscan.data.platform.utils.opencv.toGaussianBlur
import io.github.dracula101.jetscan.data.platform.utils.opencv.toHSVColorSpace
import io.github.dracula101.jetscan.data.platform.utils.opencv.toLabColorSpace
import io.github.dracula101.jetscan.data.platform.utils.opencv.toMSER
import io.github.dracula101.jetscan.data.platform.utils.opencv.toMat
import io.github.dracula101.jetscan.data.platform.utils.opencv.toMedianBlur
import io.github.dracula101.jetscan.data.platform.utils.opencv.weightedAdd
import io.github.dracula101.jetscan.data.platform.utils.permutations
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Edge
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Graph
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Intersection
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Line
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.Node
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan


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
            org.opencv.core.Size(L_KERNEL_SIZE, L_KERNEL_SIZE)
        )
        val aKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            org.opencv.core.Size(A_KERNEL_SIZE, A_KERNEL_SIZE)
        )
        lChannel = lChannel.opening(lKernel)
        aChannel = aChannel.erosion(aKernel)
        val mergedChannels = ArrayList<Mat>()
        mergedChannels.addAll(listOf(lChannel, aChannel, bChannel))
        val mergedImage = Mat()
        mergedImage.merge(mergedChannels)
        return mergedImage
    }

    private fun sortPoints(points: List<Point>): List<Point> {
        val sortedPoints = mutableListOf<Point>()
        val sortedBySum = points.sortedBy { it.x.toInt() + it.y.toInt() }
        val sortedByDiff = points.sortedBy { it.x.toInt() - it.y.toInt() }
        sortedPoints.add(sortedBySum[0])
        sortedPoints.add(sortedByDiff[0])
        sortedPoints.add(sortedBySum[1])
        sortedPoints.add(sortedByDiff[1])
        return sortedPoints
    }

    private fun hull2Points(hull: MatOfInt, contour: MatOfPoint): MatOfPoint {
        val indexes = hull.toList()
        val points: MutableList<Point> = ArrayList<Point>()
        val ctrList: List<Point> = contour.toList()
        for (index in indexes) {
            points.add(ctrList[index])
        }
        val point = MatOfPoint()
        point.fromList(points)
        return point
    }

    private fun computeLargestContours(contours: List<MatOfPoint>): List<MatOfPoint> {
        val hullList = mutableListOf<MatOfPoint>()
        for (contour in contours) {
            val hullIndex = contour.convexHull()
            val hull = hull2Points(hullIndex, contour)
            hullList.add(hull)
        }
        // Sort contours by area
        val sortedContours = hullList.sortedByDescending { Imgproc.contourArea(it) }
        return sortedContours.take(NUM_TOP_CONTOURS)
    }

    private fun findBiggestQuadrilateral(contours: List<MatOfPoint>): MatOfPoint? {
        for (contour in contours) {
            val matOfPoint2f = MatOfPoint2f(*contour.toArray())
            val peri = Imgproc.arcLength(matOfPoint2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(matOfPoint2f, approx, 0.02 * peri, true)
            val points = approx.toArray()
            if (points.size == 4) {
                val sortedPoints = sortPoints(points.toList())
                return MatOfPoint(*sortedPoints.toTypedArray())
            }
        }
        return null
    }

    private fun getDistance(point1: Point, point2: Point): Double {
        return sqrt((point1.x - point2.x).pow(2.0) + (point1.y - point2.y).pow(2.0))
    }

    private fun checkForAngle(cropCoords: ImageCropCoords): Boolean {
        val topLeft = cropCoords.topLeft
        val topRight = cropCoords.topRight
        val bottomLeft = cropCoords.bottomLeft
        val bottomRight = cropCoords.bottomRight
        val angle1 = Math.toDegrees(atan2(topRight.y - topLeft.y, topRight.x - topLeft.x))
        val angle2 = Math.toDegrees(atan2(bottomRight.y - topRight.y, bottomRight.x - topRight.x))
        val angle3 =
            Math.toDegrees(atan2(bottomLeft.y - bottomRight.y, bottomLeft.x - bottomRight.x))
        val angle4 = Math.toDegrees(atan2(topLeft.y - bottomLeft.y, topLeft.x - bottomLeft.x))
        return angle1 in MIN_ANGLE_THRESHOLD..MAX_ANGLE_THRESHOLD
                && angle2 in MIN_ANGLE_THRESHOLD..MAX_ANGLE_THRESHOLD
                && angle3 in MIN_ANGLE_THRESHOLD..MAX_ANGLE_THRESHOLD
                && angle4 in MIN_ANGLE_THRESHOLD..MAX_ANGLE_THRESHOLD
    }

    private fun checkForSide(cropCoords: ImageCropCoords): Boolean {
        val topLeft = cropCoords.topLeft
        val topRight = cropCoords.topRight
        val bottomLeft = cropCoords.bottomLeft
        val bottomRight = cropCoords.bottomRight
        val topDistance = getDistance(topLeft.toPoint(), topRight.toPoint())
        val leftDistance = getDistance(topLeft.toPoint(), bottomLeft.toPoint())
        val rightDistance = getDistance(topRight.toPoint(), bottomRight.toPoint())
        val bottomDistance = getDistance(bottomLeft.toPoint(), bottomRight.toPoint())
        val allDistances = listOf(topDistance, leftDistance, rightDistance, bottomDistance)
        val firstTwo = allDistances.sorted().take(2)
        val lastTwo = allDistances.sorted().takeLast(2)
        val firstTwoDiff = firstTwo[1] - firstTwo[0]
        val lastTwoDiff = lastTwo[1] - lastTwo[0]
        return firstTwoDiff < 0.1 * (firstTwo[0] + firstTwo[1]) && lastTwoDiff < 0.1 * (lastTwo[0] + lastTwo[1])
    }


    private fun checkQuadrilateral(cropCoords: ImageCropCoords): Boolean {
        val isSideValid = checkForSide(cropCoords)
        val isQuadrilateral = cropCoords.isQuadrilateral()
        Timber.d("isQuadrilateral: $isQuadrilateral")
        return isQuadrilateral && isSideValid
    }

    private fun MatOfPoint.toCropPoint(): ImageCropCoords {
        val points = this.toArray()
        return ImageCropCoords(
            topLeft = CPoint(points[0].x, points[0].y),
            topRight = CPoint(points[1].x, points[1].y),
            bottomLeft = CPoint(points[2].x, points[2].y),
            bottomRight = CPoint(points[3].x, points[3].y)
        )
    }

    private fun MatOfPoint2f.toCropPoint(): ImageCropCoords {
        val points = this.toArray()
        return ImageCropCoords(
            topLeft = CPoint(points[0].x, points[0].y),
            topRight = CPoint(points[1].x, points[1].y),
            bottomLeft = CPoint(points[2].x, points[2].y),
            bottomRight = CPoint(points[3].x, points[3].y)
        )
    }

    override fun detectDocument(
        imageBitmap: Bitmap,
        imageSize: Size,
        onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit
    ): ImageCropCoords? {
        val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
        val resizedHeight = (IMAGE_WIDTH / aspectRatio).toInt()
        val resizedBitmap =
            Bitmap.createScaledBitmap(imageBitmap, IMAGE_WIDTH, resizedHeight, false)
        val mat = resizedBitmap.toMat()
        val labImage = computeLABFilter(mat)
        val blurredImage = labImage.toGaussianBlur(kernelSize = BLUR_KERNEL_SIZE)
        val closingKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            org.opencv.core.Size(CLOSING_SIZE, CLOSING_SIZE)
        )
        val closedImage = blurredImage.closing(closingKernel)
        val weightedImage = closedImage.weightedAdd(WEIGHTAGE_VALUE)
        val cannedDetection = weightedImage.cannyEdge(
            minThreshold = CANNY_LOWER_THRESHOLD,
            maxThreshold = CANNY_UPPER_THRESHOLD
        )
        val dilatedKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            org.opencv.core.Size(DILATION_SIZE, DILATION_SIZE)
        )
        val dilatedImage = cannedDetection.dilation(dilatedKernel)
        onPreviewBitmapReady(labImage.toBitmap(), dilatedImage.toBitmap())
        val contours = dilatedImage.contourDetection()
        val topAreaContours = if (contours.isNotEmpty()) {
            computeLargestContours(contours)
        } else {
            return null
        }
        val bestFitContour = findBiggestQuadrilateral(topAreaContours)
        val upscaleHeight = (imageBitmap.height.toFloat() / resizedHeight.toFloat())
        val upscaleWidth = (imageBitmap.width.toFloat() / IMAGE_WIDTH.toFloat())
        val cropCoords = bestFitContour?.toCropPoint()
        // Release resources
        releaseResources(
            listOf(
                mat,
                labImage,
                blurredImage,
                closedImage,
                weightedImage,
                cannedDetection,
                dilatedImage
            )
        )
        releasePointResource(topAreaContours + listOfNotNull(bestFitContour))
        return cropCoords?.scale(upscaleWidth, upscaleHeight)
    }


    private fun textRemoval(image: Mat): Mat {
        val mserRegions = image.toMSER()
        // Remove text regions
        if (mserRegions.rows() == 0) return image
        for (i in 0 until mserRegions.rows()) {
            for (j in 0 until mserRegions.cols()) {
                val x = mserRegions.get(i, j)[0]
                val y = mserRegions.get(i, j)[1]
                image.put(y.toInt(), x.toInt(), 0.0)
            }
        }
        return image
    }

    private fun isDuplicateLine(line: Line, lines: List<Line>, threshold: Int): Boolean {
        return lines.any { abs(abs(it.slope) - abs(line.slope)) < threshold }
    }

    private fun houghLinesDetection(image: Mat, uniqueLineThreshold: Int = 30): List<Line> {
        val lines = image.houghTransform()
        // check if not empty
        if (lines.rows() == 0) return emptyList()
        val linesArray = mutableListOf<Pair<Double, Double>>()
        for (i in 0 until lines.rows()) {
            val rho = lines.get(i, 0)[0]
            val theta = lines.get(i, 0)[1]
            linesArray.add(Pair(rho, theta))
        }
        val groupUniqueLines = mutableListOf<Line>()
        val sortedLines = linesArray.sortedBy { it.first }
        for (line in sortedLines) {
            val isSimilar = isDuplicateLine(Line(line.first, line.second), groupUniqueLines, uniqueLineThreshold)
            if (!isSimilar) {
                val slope = -1 / tan(line.second)
                val yIntercept = line.first / sin(line.second)
                groupUniqueLines.add(Line(slope, yIntercept))
            }
        }
        return groupUniqueLines
    }

    private fun computeIntersections(
        lines: List<Line>,
        height: Int,
    ): List<Intersection> {
        val intersections = mutableListOf<Intersection>()
        val combinations = permutations(lines).toList().map { it.first() to it.last() }
        var vertexIndex = 0
        for (combination in combinations) {
            val line1 = combination.first
            val line2 = combination.second
            val intersection = line1.intersection(line2)
            val angle = line1.angle(line2)
            val constraintCheck = (intersection.x >= 0 || intersection.x <= IMAGE_WIDTH) || (intersection.y >= 0 || intersection.y <= height)
            val angleCheck = angle < MIN_ANGLE_THRESHOLD
            val duplicateCheck = intersections.any { it.point == intersection }
            Timber.d("Angle: $angle, Constraint: $constraintCheck, Duplicate: $duplicateCheck")
            //if (!constraintCheck || !duplicateCheck) continue
            intersections.add(
                Intersection(vertexIndex++,Pair(line1, line2),intersection)
            )
        }
        return intersections
    }

    private fun commonLineExits(line1: Line, line2: Line): Boolean {
        val set1 = setOf(line1, line2)
        val set2 = setOf(line2, line1)
        return set1 == set2
    }

    private fun generateGraph(intersections: List<Intersection>): Graph {
        val graph = Graph()
        val nodes = intersections.map { Node(it.id, it.point) }
        val edges = mutableListOf<Edge>()
        for (i in intersections.indices) {
            for (j in i + 1 until intersections.size) {
                if (commonLineExits(intersections[i].lines.first, intersections[j].lines.first)) {
                    edges.add(Edge(Pair(nodes[i], nodes[j])))
                }
            }
        }
        nodes.forEach { graph.addNode(it) }
        edges.forEach { graph.addEdge(it) }
        return graph
    }

    private fun boundedDFS(neighbors: Graph, current: Node, loops: MutableList<List<Node>>, seen: MutableSet<Node> = mutableSetOf()) {
        if (current in seen) return
        seen.add(current)
        if (seen.size == 4) {
            val dfsNeighbors = neighbors.depthFirstSearch(current, seen.last())
            if (seen.first() in dfsNeighbors) {
                loops.add(dfsNeighbors)
            }
        } else {
            for (neighbor in neighbors.getNodes()) {
                boundedDFS(neighbors, neighbor, loops, seen)
            }
        }
        seen.remove(current)
    }

    private fun findQuadrilateral(intersections: List<Intersection>): ImageCropCoords? {
        val graph = generateGraph(intersections)
        val loops = mutableListOf<List<Node>>()
        for (node in graph.getNodes()) {
            boundedDFS(graph, node, loops)
        }
        val cropPoints = loops.map { loop -> loop.map { it.point } }
        if (cropPoints.isEmpty()) return null
        val areas = cropPoints.map { ImageCropCoords.fromList(it).area() }
        val maxArea = areas.maxOrNull() ?: 0.0
        val index = areas.indexOfFirst { it == maxArea }
        return ImageCropCoords.fromList(cropPoints[index])
    }

    private fun computeQuadrilaterial(intersections: List<Intersection>): ImageCropCoords? {
        if (intersections.size < 4) return null
        val points = intersections.map { it.point }
        return ImageCropCoords.fromList(points)
    }

    override fun detectSingleDocument(
        imageBitmap: Bitmap,
        imageSize: Size,
        onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit
    ): ImageCropCoords? {
        val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
        val resizedHeight = (IMAGE_WIDTH / aspectRatio).toInt()
        val resizedBitmap =
            Bitmap.createScaledBitmap(imageBitmap, IMAGE_WIDTH, resizedHeight, true)
        val mat = resizedBitmap.toMat()
        val invertedImage = mat.bitwiseNot()
        val saturationImage = invertedImage.toHSVColorSpace()
        val blurredImage = saturationImage.toMedianBlur(kernelSize = BLUR_KERNEL_SIZE)
        val cannyEdgeImage = blurredImage.cannyEdge(
            minThreshold = CANNY_LOWER_THRESHOLD,
            maxThreshold = CANNY_UPPER_THRESHOLD
        )
        val removedText = textRemoval(cannyEdgeImage)
        val houghLines = houghLinesDetection(removedText)
        onPreviewBitmapReady(blurredImage.toBitmap(), cannyEdgeImage.toBitmap())
        val intersections = computeIntersections(houghLines, resizedHeight)
        val quadrilateral = computeQuadrilaterial(intersections)
        Timber.i(
            "HoughLines: ${houghLines.size}, Intersections: ${intersections.size}, Quadrilateral: $quadrilateral"
        )
        releaseResources(listOf(mat, invertedImage, saturationImage, blurredImage, cannyEdgeImage))
        val upscaleHeight = (imageBitmap.height.toFloat() / resizedHeight.toFloat())
        val upscaleWidth = (imageBitmap.width.toFloat() / IMAGE_WIDTH.toFloat())
        return quadrilateral?.scale(upscaleWidth, upscaleHeight)
    }

    private fun releaseResources(mats: List<Mat>) {
        for (mat in mats) {
            mat.release()
        }
    }

    private fun releasePointResource(points: List<MatOfPoint>) {
        for (point in points) {
            point.release()
        }
    }


    override fun cropDocument(imageBitmap: Bitmap, imageCropCoords: ImageCropCoords): Bitmap {
        val mat = imageBitmap.toMat()
        val topLeft = imageCropCoords.topLeft.toPoint()
        val topRight = imageCropCoords.topRight.toPoint()
        val bottomLeft = imageCropCoords.bottomLeft.toPoint()
        val bottomRight = imageCropCoords.bottomRight.toPoint()
        val width = maxOf(getDistance(topLeft, topRight),getDistance(bottomLeft, bottomRight))
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
        return croppedImage.toBitmap().also{
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

    companion object {
        private const val BLUR_KERNEL_SIZE = 7f
        private const val L_KERNEL_SIZE = 7.0
        private const val A_KERNEL_SIZE = 3.0
        private const val WEIGHTAGE_VALUE = 0.8
        private const val CLOSING_SIZE = 7.0
        private const val DILATION_SIZE = 3.0

        // private const val CANNY_LOWER_THRESHOLD = 60.0
        private const val CANNY_LOWER_THRESHOLD = 100.0
        private const val CANNY_UPPER_THRESHOLD = 200.0
        private const val IMAGE_WIDTH = 250
        private const val NUM_TOP_CONTOURS = 10
        private const val MIN_ANGLE_THRESHOLD = 45.0
        private const val MAX_ANGLE_THRESHOLD = 150.0
    }

}