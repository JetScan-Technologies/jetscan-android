package io.github.dracula101.jetscan.data.document.models.image


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize
import java.io.File

@Immutable
@Stable
@Parcelize
data class ScannedImage(
    val size: Long,
    val scannedUri: Uri,
    val date: Long,
    val width: Int,
    val height: Int,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM
) : Parcelable {
    fun copy(
        size: Long? = null,
        scannedUri: Uri? = null,
        date: Long? = null,
        width: Int? = null,
        height: Int? = null,
        imageQuality: ImageQuality? = null
    ): ScannedImage {
        return ScannedImage(
            size = size ?: this.size,
            scannedUri = scannedUri ?: this.scannedUri,
            date = date ?: this.date,
            width = width ?: this.width,
            height = height ?: this.height,
            imageQuality = imageQuality ?: this.imageQuality
        )
    }

    companion object {

        fun fromBitmap(
            bitmap: Bitmap,
            path: String,
            quality: ImageQuality,
            name: String
        ): ScannedImage {
            val file = File(path, "$name.png")
            if (!File(path).exists()) {
                File(path).mkdirs()
            }
            return ScannedImage(
                size = file.length(),
                scannedUri = file.toUri(),
                date = System.currentTimeMillis(),
                width = bitmap.width,
                height = bitmap.height,
                imageQuality = quality
            )
        }

        fun fromFile(file: File): ScannedImage {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            return ScannedImage(
                size = file.length(),
                scannedUri = file.toUri(),
                date = System.currentTimeMillis(),
                width = bitmap.width,
                height = bitmap.height
            )
        }
    }
}
