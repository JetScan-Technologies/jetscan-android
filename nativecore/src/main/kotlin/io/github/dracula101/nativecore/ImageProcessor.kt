package io.github.dracula101.nativecore

import android.graphics.Bitmap

/**
 * Replacement interface for OpenCvManager.
 * All methods run on the calling thread (use coroutines/dispatchers in ViewModel).
 */
interface ImageProcessor {

    /** @return true if native library loaded successfully */
    fun initialize(): Boolean

    /** Detect document corners using per-channel Canny + contour approach (Path A) */
    fun detectDocument(bitmap: Bitmap): ImageCropCoords?

    /** Detect document using LAB + HoughLinesP approach (Path B) */
    fun detectSingleDocument(bitmap: Bitmap): ImageCropCoords?

    /** Detect lines for live camera overlay */
    fun getLines(bitmap: Bitmap): List<Line>

    /** Perspective-crop document from bitmap using detected corners */
    fun cropDocument(bitmap: Bitmap, coords: ImageCropCoords): Bitmap

    /** Rotate document by degrees (positive = clockwise) */
    fun rotateDocument(bitmap: Bitmap, degrees: Float): Bitmap

    /** Apply all 7 filters and return one bitmap per filter */
    fun applyFilters(bitmap: Bitmap): List<Bitmap>

    /** Apply a single named filter */
    fun applyFilter(bitmap: Bitmap, filter: ImageFilter): Bitmap

    /** Apply color adjustment: brightness [-255,255], contrast [0,10], saturation [0,5] */
    fun applyColorAdjustment(bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap

    /** Convert NV21 YUV camera frame to RGBA Bitmap */
    fun convertYuvFrame(nv21: ByteArray, width: Int, height: Int): Bitmap
}

/** Mirrors nc::ImageFilter enum (same ordinal order) */
enum class ImageFilter {
    ORIGINAL,
    VIBRANT,
    NO_SHADOW,
    AUTO,
    COLOR_BUMP,
    GRAYSCALE,
    B_W
}

/** Document corner coordinates */
data class ImageCropCoords(
    val topLeft:     FloatPair,
    val topRight:    FloatPair,
    val bottomLeft:  FloatPair,
    val bottomRight: FloatPair
)

data class FloatPair(val x: Float, val y: Float)

/** Slope-intercept line */
data class Line(val slope: Float, val yIntercept: Float)
