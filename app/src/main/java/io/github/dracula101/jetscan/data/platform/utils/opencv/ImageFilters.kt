package io.github.dracula101.jetscan.data.platform.utils.opencv

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/*

fun Mat.colorBump(): Mat {
    val hsvMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(this, hsvMat, org.opencv.imgproc.Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    org.opencv.core.Core.split(hsvMat, hsvChannels)
    val hueMat = hsvChannels[0]
    val saturationMat = hsvChannels[1]
    val valueMat = hsvChannels[2]
    val alpha = 0.5

    val bumpMat = Mat()
    org.opencv.core.Core.addWeighted(hueMat, alpha, saturationMat, 1.0 - alpha, 0.0, bumpMat)
    org.opencv.core.Core.merge(listOf(bumpMat, saturationMat, valueMat), hsvMat)
    val bumpRgbMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(hsvMat, bumpRgbMat, org.opencv.imgproc.Imgproc.COLOR_HSV2RGB)
    return bumpRgbMat
}

*/
/*
* Brighten Effect
* *//*

fun Mat.brighten(value: Double = 50.0): Mat {
    val hsvMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(this, hsvMat, org.opencv.imgproc.Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    org.opencv.core.Core.split(hsvMat, hsvChannels)
    val valueMat = hsvChannels[2]
    val alpha = 1.0
    val beta = value

    val brightenedMat = Mat()
    org.opencv.core.Core.addWeighted(valueMat, alpha, valueMat, beta, 0.0, brightenedMat)
    org.opencv.core.Core.merge(listOf(hsvChannels[0], hsvChannels[1], brightenedMat), hsvMat)
    val brightenedRgbMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(hsvMat, brightenedRgbMat, org.opencv.imgproc.Imgproc.COLOR_HSV2RGB)
    return brightenedRgbMat
}

*/
/*
* Color Filter
 *//*

fun Mat.colorHalftone(): Mat {
    val hsvMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(this, hsvMat, org.opencv.imgproc.Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    org.opencv.core.Core.split(hsvMat, hsvChannels)
    val hueMat = hsvChannels[0]
    val saturationMat = hsvChannels[1]
    val valueMat = hsvChannels[2]

    val halftoneMat = Mat()
    org.opencv.core.Core.addWeighted(hueMat, 0.5, saturationMat, 0.5, 0.0, halftoneMat)
    org.opencv.core.Core.merge(listOf(halftoneMat, halftoneMat, valueMat), hsvMat)
    val halftoneRgbMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(hsvMat, halftoneRgbMat, org.opencv.imgproc.Imgproc.COLOR_HSV2RGB)
    return halftoneRgbMat
}

fun Mat.colorize(hue: Double = 0.0, saturation: Double = 0.0, value: Double = 0.0): Mat {
    val hsvMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(this, hsvMat, org.opencv.imgproc.Imgproc.COLOR_RGB2HSV)
    val hsvChannels = mutableListOf<Mat>()
    org.opencv.core.Core.split(hsvMat, hsvChannels)
    val hueMat = hsvChannels[0]
    val saturationMat = hsvChannels[1]
    val valueMat = hsvChannels[2]

    val colorizedMat = Mat()
    org.opencv.core.Core.addWeighted(hueMat, hue, saturationMat, saturation, 0.0, colorizedMat)
    org.opencv.core.Core.addWeighted(colorizedMat, 1.0, valueMat, value, 0.0, colorizedMat)
    org.opencv.core.Core.merge(listOf(colorizedMat, saturationMat, valueMat), hsvMat)
    val colorizedRgbMat = Mat()
    org.opencv.imgproc.Imgproc.cvtColor(hsvMat, colorizedRgbMat, org.opencv.imgproc.Imgproc.COLOR_HSV2RGB)
    return colorizedRgbMat
}

fun Mat.contrast(alpha: Double = 1.0, beta: Double = 0.0): Mat {
    val contrastMat = Mat()
    org.opencv.core.Core.convertScaleAbs(this, contrastMat, alpha, beta)
    return contrastMat
}

fun Mat.diffuse(): Mat {
    val diffuseMat = Mat()
    org.opencv.photo.Photo.edgePreservingFilter(this, diffuseMat)
    return diffuseMat
}

fun Mat.sharpen(): Mat {
    val sharpenMat = Mat()
    org.opencv.imgproc.Imgproc.GaussianBlur(this, sharpenMat, org.opencv.core.Size(0.0, 0.0), 3.0)
    org.opencv.core.Core.addWeighted(this, 1.5, sharpenMat, -0.5, 0.0, sharpenMat)
    return sharpenMat
}

fun Mat.vignette(): Mat {
    val vignetteMat = Mat()
    org.opencv.imgproc.Imgproc.GaussianBlur(this, vignetteMat, org.opencv.core.Size(0.0, 0.0), 7.0)
    org.opencv.core.Core.addWeighted(this, 0.7, vignetteMat, 0.3, 0.0, vignetteMat)
    return vignetteMat
}*/

fun Mat.original(): Mat {
    return this.clone()
}

fun Mat.grayscale(): Mat {
    val grayImage = Mat()
    Imgproc.cvtColor(this, grayImage, Imgproc.COLOR_BGR2GRAY)
    return grayImage
}

fun Mat.colorBump(alpha: Double = 1.5, beta: Double = 0.0): Mat {
    val bumpedImage = Mat()
    this.convertTo(bumpedImage, -1, alpha, beta)
    return bumpedImage
}

fun Mat.brighten(beta: Double = 50.0): Mat {
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