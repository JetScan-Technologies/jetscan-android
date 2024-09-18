package io.github.dracula101.jetscan.data.platform.utils.opencv

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.features2d.FeatureDetector
import org.opencv.imgproc.Imgproc

/*
* Morphological Operations
 */
fun Mat.morphology(kernelSize: Double, operation: Int, anchor: Point = Point(-1.0, -1.0), iterations: Int = 1): Mat {
    val morphedMat = Mat()
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, org.opencv.core.Size(kernelSize, kernelSize))
    Imgproc.morphologyEx(this, morphedMat,  operation, kernel, anchor, iterations)
    return morphedMat
}

/*
* Opening Operation
 */
fun Mat.opening(kernel: Mat, iterations: Int = 1): Mat {
    val openedMat = Mat()
    org.opencv.imgproc.Imgproc.morphologyEx(this, openedMat, org.opencv.imgproc.Imgproc.MORPH_OPEN, kernel, Point(-1.0, -1.0), iterations)
    return openedMat
}

/*
* Closing Operation
 */
fun Mat.closing(kernel: Mat, iterations: Int = 1): Mat {
    val closedMat = Mat()
    org.opencv.imgproc.Imgproc.morphologyEx(this, closedMat, org.opencv.imgproc.Imgproc.MORPH_CLOSE, kernel, Point(-1.0, -1.0), iterations)
    return closedMat
}

fun Mat.closing(kernelSize : Double = 5.0, iterations: Int = 1): Mat {
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(kernelSize, kernelSize))
    return closing(kernel, iterations)
}


/*
* Dilation Operation
 */
fun Mat.dilation(kernel: Mat): Mat {
    val dilatedMat = Mat()
    org.opencv.imgproc.Imgproc.dilate(this, dilatedMat, kernel)
    return dilatedMat
}

/*
* Erosion Operation
 */
fun Mat.erosion(kernel: Mat, iterations: Int = 1): Mat {
    val erodedMat = Mat()
    org.opencv.imgproc.Imgproc.erode(this, erodedMat, kernel, Point(-1.0, -1.0), iterations)
    return erodedMat
}

/*
* Weightage Operation
 */
fun Mat.weightedAdd(weightage: Double): Mat {
    val weightedMat = Mat()
    Core.addWeighted(this, weightage, this, 0.5, 0.0, weightedMat)
    return weightedMat
}

fun Mat.split(): ArrayList<Mat>{
    val channels = ArrayList<Mat>()
    Core.split(this, channels)
    return channels
}

fun Mat.merge(channels: List<Mat>) {
    Core.merge(channels, this)
}

fun Mat.threshold(threshold: Double, maxVal: Double, type: Int): Mat {
    val thresholdMat = Mat()
    Imgproc.threshold(this, thresholdMat, threshold, maxVal, type)
    return thresholdMat
}

fun Mat.bitwiseAnd(mask: Mat): Mat {
    val result = Mat()
    Core.bitwise_and(this, mask, result)
    return result
}

fun Mat.bitwiseOr(mask: Mat): Mat {
    val result = Mat()
    Core.bitwise_or(this, mask, result)
    return result
}

fun Mat.bitwiseNot(): Mat {
    val result = Mat()
    Core.bitwise_not(this, result)
    return result
}

fun Mat.bitwiseAnd(): Mat {
    val result = Mat()
    Core.bitwise_and(this, this, result)
    return result
}

fun Mat.bitwiseXor(mask: Mat): Mat {
    val result = Mat()
    Core.bitwise_xor(this, mask, result)
    return result
}

fun Mat.toMSER(): MatOfKeyPoint {
    val mser = FeatureDetector.create(FeatureDetector.MSER)
    val keyPoints = MatOfKeyPoint()
    mser.detect(this, keyPoints)
    return keyPoints
}

fun MatOfPoint.convexHull(): MatOfInt{
    val hullIndex = MatOfInt()
    Imgproc.convexHull(this, hullIndex)
    return hullIndex
}

fun Mat.crop(
    cropPoints: MatOfPoint,
    width: Double,
    height: Double
): Mat {
    val dstPoints = MatOfPoint(
        Point(0.0, 0.0),
        Point(width, 0.0),
        Point(0.0, height),
        Point(width, height),
    )
    val cropMatPoints = MatOfPoint2f(*cropPoints.toArray())
    val dstMatPoints = MatOfPoint2f(*dstPoints.toArray())
    val perspectiveTransform = Imgproc.getPerspectiveTransform(cropMatPoints, dstMatPoints)
    val croppedMat = Mat()
    Imgproc.warpPerspective(this, croppedMat, perspectiveTransform, org.opencv.core.Size(width, height))
    return croppedMat
}

fun Mat.rotate(
    rotationDegrees: Double
): Mat {
    val rotationMatrix = Imgproc.getRotationMatrix2D(Point(this.width() / 2.0, this.height() / 2.0), rotationDegrees, 1.0)
    val rotatedMat = Mat()
    Imgproc.warpAffine(this, rotatedMat, rotationMatrix, org.opencv.core.Size(this.width().toDouble(), this.height().toDouble()))
    return rotatedMat
}
/**
 * Has to be HSV image
 */
fun Mat.gradient(kernel: Mat? = null): Mat {
    val gradient = Mat()
    val gradientKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
    Imgproc.morphologyEx(this, gradient, Imgproc.MORPH_GRADIENT, kernel ?: gradientKernel)
    return gradient
}

