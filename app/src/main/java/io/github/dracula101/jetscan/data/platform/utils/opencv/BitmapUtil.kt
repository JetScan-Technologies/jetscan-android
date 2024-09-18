package io.github.dracula101.jetscan.data.platform.utils.opencv

import android.graphics.Bitmap
import io.github.dracula101.jetscan.data.document.models.image.ImageType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

fun Bitmap.toMat(): Mat {
    val mat = Mat()
    org.opencv.android.Utils.bitmapToMat(this, mat)
    return mat
}

fun Mat.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.cols(), this.rows(), Bitmap.Config.ARGB_8888)
    org.opencv.android.Utils.matToBitmap(this, bitmap)
    return bitmap
}

fun Mat.area(): Int {
    return this.width() * this.height()
}

fun Bitmap.saveBitmapToFile(file: File, imageType: ImageType = ImageType.JPEG, quality: Int = 100) : Boolean {
    return try {
        val mat = toMat()
        Imgcodecs.imwrite(
            file.absolutePath,
            mat,
            MatOfInt(
                when (imageType) {
                    ImageType.JPEG -> Imgcodecs.IMWRITE_JPEG_QUALITY
                    ImageType.PNG -> Imgcodecs.IMWRITE_PNG_COMPRESSION
                    ImageType.JPG -> Imgcodecs.IMWRITE_JPEG_OPTIMIZE
                    ImageType.UNKNOWN -> throw IllegalArgumentException("Unknown image type")
                },
                quality
            )
        )
        mat.release()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}