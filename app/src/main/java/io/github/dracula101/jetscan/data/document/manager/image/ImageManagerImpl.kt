package io.github.dracula101.jetscan.data.document.manager.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageManagerImpl : ImageManager {

    override fun saveImageFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
        name: String,
        path: String?
    ): File? {
        return try {
            contentResolver.openInputStream(uri).use { inputStream ->
                val file = path?.let { File(it) } ?: File(name)
                val outputStream = FileOutputStream(file)
                outputStream.write(inputStream?.readBytes())
                outputStream.flush()
                file
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun saveImageFromFile(file: File, bitmap: Bitmap, quality: ImageQuality): Boolean {
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality.toBitmapQuality(), outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            false
        }
    }


    override fun compress(bitmap: Bitmap, quality: ImageQuality): Bitmap {
        val originalBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val outputStream = ByteArrayOutputStream()
        try {
            originalBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality.toBitmapQuality(),
                outputStream
            )
        } catch (e: Exception) {
            throw e
        }
        return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
    }

    override fun getBitmapFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
    ): Bitmap {
        return try {
            contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}