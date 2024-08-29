package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera

import android.graphics.Bitmap
import android.os.Parcelable
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageEffect
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class CameraScannedImage (
    val id: String = UUID.randomUUID().toString(),
    val originalImage : Bitmap,
    val croppedImage: Bitmap?,
    val filteredImage: Bitmap?,
    val imageEffect: ImageEffect,
    val imageQuality: ImageQuality,
    val cropCoords: ImageCropCoords = ImageCropCoords.fromBitmap(originalImage),
) : Parcelable {
    companion object {
        val fromBitmap: (Bitmap) -> CameraScannedImage = { image ->
            val imageSizeMB = (image.byteCount / 100000).toFloat()
            val imageQuality = when(imageSizeMB) {
                in 0.0 .. 0.2 -> ImageQuality.VERY_LOW
                in 0.2..1.0 -> ImageQuality.LOW
                in 1.0..4.0 -> ImageQuality.MEDIUM
                in 4.0..Float.MAX_VALUE.toDouble() -> ImageQuality.HIGH
                else -> ImageQuality.VERY_LOW
            }
            CameraScannedImage(
                id = UUID.randomUUID().toString(),
                image,
                null,
                null,
                ImageEffect(),
                imageQuality,
                cropCoords = ImageCropCoords.fromBitmap(image)
            )
        }
    }
}

