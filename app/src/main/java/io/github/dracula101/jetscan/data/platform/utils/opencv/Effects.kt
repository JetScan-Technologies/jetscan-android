package io.github.dracula101.jetscan.data.platform.utils.opencv

import androidx.annotation.FloatRange
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

/*
*  Hue channel
* */
fun Mat.hueChannel(): Mat {
    val hsvMat = Mat()
    Imgproc.cvtColor(this, hsvMat, Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    Core.split(hsvMat, hsvChannels)
    return hsvChannels[0]
}

/*
* Saturation channel
 */
fun Mat.saturationChannel(): Mat {
    val hsvMat = Mat()
    Imgproc.cvtColor(this, hsvMat, Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    Core.split(hsvMat, hsvChannels)
    return hsvChannels[2]
}

/*
*  Value channel
* */
fun Mat.valueChannel(): Mat {
    val hsvMat = Mat()
    Imgproc.cvtColor(this, hsvMat, Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    Core.split(hsvMat, hsvChannels)
    return hsvChannels[2]
}

/*
* Grayscale Effect
* */
fun Mat.toGrayScale(): Mat {
    val grayMat = Mat()
    Imgproc.cvtColor(this, grayMat, Imgproc.COLOR_RGB2GRAY)
    return grayMat
}


/*
* Gaussian Blur Effect
 */
fun Mat.toGaussianBlur(kernelSize: Float = 5.0f, sigma: Double= 0.0): Mat {
    val blurredMat = Mat()
    Imgproc.GaussianBlur(this, blurredMat, org.opencv.core.Size(kernelSize.toDouble(), kernelSize.toDouble()), sigma)
    return blurredMat
}

/*
* Median Blur Effect
* */
fun Mat.toMedianBlur(kernelSize: Float = 5.0f): Mat {
    val blurredMat = Mat()
    Imgproc.medianBlur(this, blurredMat, kernelSize.toInt())
    return blurredMat
}

/*
* Normalize Effect
 */
fun Mat.normalize(alpha: Double = 0.0, beta: Double = 255.0): Mat {
    val normalizedMat = Mat()
    Core.normalize(this, normalizedMat, alpha, beta, Core.NORM_MINMAX)
    return normalizedMat
}

/*
* Threshold Effect
* */
fun Mat.toThreshold(threshold: Double = 127.0, maxVal: Double = 255.0, type: Int = Imgproc.THRESH_BINARY): Mat {
    val thresholdMat = Mat()
    Imgproc.threshold(this, thresholdMat, threshold, maxVal, type)
    return thresholdMat
}


/*
* L*a*b Color Space
 */
fun Mat.toLabColorSpace(): Mat {
    val labMat = Mat()
    Imgproc.cvtColor(this, labMat, Imgproc.COLOR_RGB2Lab)
    return labMat
}

fun Mat.toHSVColorSpace(): Mat {
    val hsvMat = Mat()
    Imgproc.cvtColor(this, hsvMat, Imgproc.COLOR_RGB2HSV)
    return hsvMat
}


/*
* Canny Edge Detection
 */
fun Mat.cannyEdge(minThreshold: Double = 50.0, maxThreshold: Double = 150.0): Mat {
    val edges = Mat()
    Imgproc.Canny(this, edges, minThreshold, maxThreshold)
    return edges
}

/*
* Harris Corner Detection
 */
fun Mat.harrisCornerDetection(): Mat {
    val corners = Mat()
    Imgproc.cornerHarris(this, corners, 2, 3, 0.04)
    return corners
}

/*
* Hough Transform
* */
fun Mat.houghTransform(theta: Double = Math.PI /90, threshold: Int = 65): Mat {
    val lines = Mat()
    Imgproc.HoughLinesP(this, lines, 1.0, theta, threshold)
    return lines
}

/*
* Contour Detection
* */
fun Mat.contourDetection(hierarchy: Mat? = null): List<MatOfPoint> {
    val contours = mutableListOf<MatOfPoint>()
    Imgproc.findContours(this, contours,hierarchy?: Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
    return contours
}


/*
* Apply Filter
* */
fun Mat.applyFilter(filter: ImageFilter): Mat {
    return when (filter) {
        ImageFilter.ORIGINAL -> this
        ImageFilter.VIBRANT -> toVibrant()
        ImageFilter.NO_SHADOW -> noShadow()
        ImageFilter.AUTO -> toAutoFilter()
        ImageFilter.COLOR_BUMP -> colorBump()
        ImageFilter.GRAYSCALE -> toGrayScale()
        ImageFilter.B_W -> toBlackAndWhite()
    }
}

fun Mat.colorAdjustment(
    @FloatRange(from = -255.0, to = 255.0)
     brightness: Float = 0.0f,
    @FloatRange(from = 0.0, to = 10.0)
     contrast: Float = 1.0f,
    @FloatRange(from = 0.0, to = 5.0)
    saturation: Float = 1.0f
): Mat {
    val adjustedMat = Mat()
    this.convertTo(adjustedMat, -1, contrast.toDouble(), brightness.toDouble())
    val hsvMat = Mat()
    Imgproc.cvtColor(adjustedMat, hsvMat, Imgproc.COLOR_BGR2HSV)
    val hsvChannels = mutableListOf<Mat>()
    Core.split(hsvMat, hsvChannels)
    val saturationChannel = hsvChannels[1]
    saturationChannel.convertTo(saturationChannel, CvType.CV_32F)
    Core.multiply(saturationChannel, Scalar(saturation.toDouble()), saturationChannel)
    Core.min(saturationChannel, Scalar(255.0), saturationChannel)
    saturationChannel.convertTo(saturationChannel, CvType.CV_8U)
    Core.merge(hsvChannels, hsvMat)
    Imgproc.cvtColor(hsvMat, adjustedMat, Imgproc.COLOR_HSV2BGR)
    hsvMat.release()
    hsvChannels.forEach { it.release() }
    return adjustedMat
}