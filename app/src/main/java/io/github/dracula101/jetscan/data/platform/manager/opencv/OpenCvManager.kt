package io.github.dracula101.jetscan.data.platform.manager.opencv

import android.graphics.Bitmap
import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter

@Immutable
interface OpenCvManager {

    /*
    * Method for initializing OpenCV.
    * */
    fun initialize()

    /*
    * Method for detecting document.
     */
    fun detectDocument(
        imageBitmap: Bitmap,
        imageSize: Size,
        onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit
    ) : ImageCropCoords?

    /*
    * Method for detecting single document.
     */
    fun detectSingleDocument(
        imageBitmap: Bitmap,
        imageSize: Size,
        onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit
    ): ImageCropCoords?

    fun cropDocument(
        imageBitmap: Bitmap,
        imageCropCoords: ImageCropCoords
    ): Bitmap

    fun rotateDocument(
        imageBitmap: Bitmap,
        rotationDegrees: Float
    ): Bitmap

    suspend fun applyFilters(
        imageBitmap: Bitmap,
    ) : List<Bitmap>

    fun applyFilter(
        imageBitmap: Bitmap,
        filter: ImageFilter
    ) : Bitmap

    fun applyColorAdjustment(
        bitmap: Bitmap,
        @FloatRange(from = -255.0, to = 255.0)
        brightness: Float = 0.0f,
        @FloatRange(from = 0.0, to = 10.0)
        contrast: Float = 1.0f,
        @FloatRange(from = 0.0, to = 5.0)
        saturation: Float = 1.0f
    ): Bitmap
}