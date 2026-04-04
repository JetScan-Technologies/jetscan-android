package io.github.dracula101.nativecore

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.createBitmap

/**
 * JNI-backed implementation of [ImageProcessor].
 * All JNI calls are declared as companion object external methods to
 * keep them visible for ProGuard -keep rules.
 */
class NativeImageProcessor : ImageProcessor {

    override fun initialize(): Boolean {
        JetScanNative // trigger library load
        return try {
            nativeInit()
            true
        } catch (e: Throwable) {
            false
        }
    }

    override fun detectDocument(bitmap: Bitmap): ImageCropCoords? {
        // RGBA_8888 required by native bridge
        val rgba = ensureRgba(bitmap)
        val arr = nativeDetectDocument(rgba) ?: return null
        return arr.toCoords()
    }

    override fun detectSingleDocument(bitmap: Bitmap): ImageCropCoords? {
        val rgba = ensureRgba(bitmap)
        val arr = nativeDetectSingleDocument(rgba) ?: return null
        return arr.toCoords()
    }

    override fun getLines(bitmap: Bitmap): List<Line> {
        val rgba = ensureRgba(bitmap)
        val arr = nativeGetLines(rgba)
        val result = mutableListOf<Line>()
        var i = 0
        while (i + 1 < arr.size) {
            result.add(Line(arr[i], arr[i + 1]))
            i += 2
        }
        return result
    }

    override fun cropDocument(bitmap: Bitmap, coords: ImageCropCoords): Bitmap {
        val rgba = ensureRgba(bitmap)
        val corners = floatArrayOf(
            coords.topLeft.x,     coords.topLeft.y,
            coords.topRight.x,    coords.topRight.y,
            coords.bottomLeft.x,  coords.bottomLeft.y,
            coords.bottomRight.x, coords.bottomRight.y
        )
        return nativeCropDocument(rgba, corners)
            ?: bitmap
    }

    override fun rotateDocument(bitmap: Bitmap, degrees: Float): Bitmap {
        val rgba = ensureRgba(bitmap)
        return nativeRotateDocument(rgba, degrees) ?: bitmap
    }

    override fun applyFilters(bitmap: Bitmap): List<Bitmap> {
        val rgba = ensureRgba(bitmap)
        val arr = nativeApplyFilters(rgba) ?: return emptyList()
        return arr.toList()
    }

    override fun applyFilter(bitmap: Bitmap, filter: ImageFilter): Bitmap {
        val rgba = ensureRgba(bitmap)
        return nativeApplyFilter(rgba, filter.ordinal) ?: bitmap
    }

    override fun applyColorAdjustment(bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val rgba = ensureRgba(bitmap)
        return nativeApplyColorAdjustment(rgba, brightness, contrast, saturation) ?: bitmap
    }

    override fun convertYuvFrame(nv21: ByteArray, width: Int, height: Int): Bitmap {
        return nativeConvertYuvFrame(nv21, width, height)
            ?: createBitmap(width, height)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun ensureRgba(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
        else bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun FloatArray.toCoords(): ImageCropCoords = ImageCropCoords(
        topLeft     = FloatPair(this[0], this[1]),
        topRight    = FloatPair(this[2], this[3]),
        bottomLeft  = FloatPair(this[4], this[5]),
        bottomRight = FloatPair(this[6], this[7])
    )

    // ── JNI declarations ───────────────────────────────────────────────────

    companion object {
        @JvmStatic private external fun nativeInit()
        @JvmStatic external fun nativeDetectDocument(bitmap: Bitmap): FloatArray?
        @JvmStatic external fun nativeDetectSingleDocument(bitmap: Bitmap): FloatArray?
        @JvmStatic external fun nativeGetLines(bitmap: Bitmap): FloatArray
        @JvmStatic external fun nativeCropDocument(bitmap: Bitmap, corners: FloatArray): Bitmap?
        @JvmStatic external fun nativeRotateDocument(bitmap: Bitmap, degrees: Float): Bitmap?
        @JvmStatic external fun nativeApplyFilters(bitmap: Bitmap): Array<Bitmap>?
        @JvmStatic external fun nativeApplyFilter(bitmap: Bitmap, filterOrdinal: Int): Bitmap?
        @JvmStatic external fun nativeApplyColorAdjustment(
            bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float
        ): Bitmap?
        @JvmStatic external fun nativeConvertYuvFrame(nv21: ByteArray, width: Int, height: Int): Bitmap?
    }
}
