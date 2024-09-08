package io.github.dracula101.jetscan.data.platform.utils.opencv

import android.graphics.Bitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

fun Mat.grayscale(): Mat {
    val grayImage = Mat()
    Imgproc.cvtColor(this, grayImage, Imgproc.COLOR_BGR2GRAY)
    return grayImage
}

fun Mat.autoEnhance(): Mat {
    val autoEnhancedImage = Mat()
    Core.normalize(this, autoEnhancedImage, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
    return autoEnhancedImage
}

fun Mat.noShadow(): Mat {
    val gray = this.grayscale()
    val blurred = gray.toGaussianBlur(15.0f, 0.0)
    val corrected = Mat()
    Core.divide(gray, blurred, corrected, 255.0)
    val thresholded = Mat()
    Imgproc.adaptiveThreshold(
        corrected,
        thresholded,
        255.0,
        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
        Imgproc.THRESH_BINARY,
        15, // Block size to adjust thresholding
        10.0 // Constant to fine-tune shadow removal
    )
    val bilateralFiltered = Mat()
    Imgproc.bilateralFilter(thresholded, bilateralFiltered, 9, 75.0, 75.0)
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(1.0, 1.0))
    val morphCleaned = Mat()
    Imgproc.morphologyEx(bilateralFiltered, morphCleaned, Imgproc.MORPH_CLOSE, kernel)
    val sharpenKernel = MatOfFloat(
        -1f, -1f, -1f,
        -1f,  9f, -1f,
        -1f, -1f, -1f
    )
    val sharpened = Mat()
    Imgproc.filter2D(morphCleaned, sharpened, -1, sharpenKernel)
    gray.release()
    blurred.release()
    corrected.release()
    thresholded.release()
    bilateralFiltered.release()
    morphCleaned.release()
    return sharpened
}

fun Mat.toVibrant(scaleFactor: Double = 2.0): Mat {
    val hsv = Mat()
    Imgproc.cvtColor(this, hsv, Imgproc.COLOR_BGR2HSV)
    val channels = mutableListOf<Mat>()
    Core.split(hsv, channels)
    val s = channels[1]
    Core.multiply(s, Scalar(scaleFactor), s)
    Core.normalize(s, s, 0.0, 255.0, Core.NORM_MINMAX)
    Core.merge(channels, hsv)
    val vibrant = Mat()
    Imgproc.cvtColor(hsv, vibrant, Imgproc.COLOR_HSV2BGR)
    hsv.release()
    return vibrant
}



fun Mat.toAutoFilter(): Mat {
    // constrast enhancement
    val contrastImage = Mat()
    this.convertTo(contrastImage, -1, 2.0, 0.0)
    // color bump
    val bumpedImage = Mat()
    contrastImage.convertTo(bumpedImage, -1, 1.5, 0.0)
    // no shadow
    val gray = bumpedImage.grayscale()
    val blurred = gray.toGaussianBlur(15.0f, 0.0)
    val corrected = Mat()
    Core.divide(gray, blurred, corrected, 255.0)
    val thresholded = Mat()
    Imgproc.adaptiveThreshold(
        corrected,
        thresholded,
        255.0,
        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
        Imgproc.THRESH_BINARY,
        15, // Block size to adjust thresholding
        10.0 // Constant to fine-tune shadow removal
    )
    val bilateralFiltered = Mat()
    Imgproc.bilateralFilter(thresholded, bilateralFiltered, 9, 75.0, 75.0)
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(1.0, 1.0))
    val morphCleaned = Mat()
    Imgproc.morphologyEx(bilateralFiltered, morphCleaned, Imgproc.MORPH_CLOSE, kernel)
    val sharpenKernel = MatOfFloat(
        -1f, -1f, -1f,
        -1f,  9f, -1f,
        -1f, -1f, -1f
    )
    val sharpened = Mat()
    Imgproc.filter2D(morphCleaned, sharpened, -1, sharpenKernel)
    gray.release()
    blurred.release()
    corrected.release()
    thresholded.release()
    bilateralFiltered.release()
    morphCleaned.release()
    return sharpened
}

fun Mat.colorBump(alpha: Double = 1.5, beta: Double = 0.0): Mat {
    val bumpedImage = Mat()
    this.convertTo(bumpedImage, -1, alpha, beta)
    return bumpedImage
}

fun Mat.toBlackAndWhite(): Mat {
    val grayImage = this.grayscale()
    val blackAndWhiteImage = Mat()
    Imgproc.threshold(grayImage, blackAndWhiteImage, 128.0, 255.0, Imgproc.THRESH_BINARY)
    return blackAndWhiteImage
}

fun Mat.sharpBlack(): Mat {
    // sharpen and convert to grayscale
    val sharpened = this.sharpen()
    val gray = sharpened.grayscale()
    sharpened.release()
    // threshold and convert to black and white
    val thresholded = Mat()
    Imgproc.threshold(gray, thresholded, 128.0, 255.0, Imgproc.THRESH_BINARY)
    gray.release()
    return thresholded
}

fun Mat.brighten(beta: Double = 10.0): Mat {
    val brightenedImage = Mat()
    this.convertTo(brightenedImage, -1, 1.0, beta)
    return brightenedImage
}

fun Mat.colorHalftone(): Mat {
    val grayImage = this.grayscale()
    val halftoneImage = Mat()
    Imgproc.adaptiveThreshold(grayImage, halftoneImage, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
    return halftoneImage
}

fun Mat.colorize(colormap: Int = Imgproc.COLORMAP_JET): Mat {
    val colorizedImage = Mat()
    Imgproc.applyColorMap(this, colorizedImage, colormap)
    return colorizedImage
}

fun Mat.contrast(alpha: Double = 2.0): Mat {
    val contrastImage = Mat()
    this.convertTo(contrastImage, -1, alpha, 0.0)
    return contrastImage
}

fun Mat.diffuse(ksize: Size = Size(15.0, 15.0)): Mat {
    val diffusedImage = Mat()
    Imgproc.GaussianBlur(this, diffusedImage, ksize, 0.0)
    return diffusedImage
}

fun Mat.sharpen(): Mat {
    val kernel = Mat(3, 3, CvType.CV_32F)
    kernel.put(0, 0, 0.0, -1.0, 0.0, -1.0, 5.0, -1.0, 0.0, -1.0, 0.0)
    val sharpenedImage = Mat()
    Imgproc.filter2D(this, sharpenedImage, -1, kernel)
    return sharpenedImage
}