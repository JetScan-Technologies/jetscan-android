package io.github.dracula101.jetscan.data.platform.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat

fun Bitmap.aspectRatio() : Float {
    return this.width.toFloat() / this.height.toFloat()
}

fun Bitmap.toBase64(imageQuality: ImageQuality = ImageQuality.MEDIUM): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, imageQuality.toBitmapQuality(), byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun Bitmap.compress(imageQuality: ImageQuality = ImageQuality.MEDIUM): Bitmap {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, imageQuality.toBitmapQuality(), byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.scaleDown(scaleX: Float, scaleY: Float): Bitmap {
    val matrix = Matrix()
    matrix.postScale(scaleX, scaleY)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.scaleUp(scaleX: Float, scaleY: Float): Bitmap {
    val matrix = Matrix()
    matrix.postScale(scaleX, scaleY)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.crop(x: Int, y: Int, width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(this, x, y, width, height)
}

fun Bitmap.resize(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, false)
}

fun Bitmap.changeQuality(imageQuality: ImageQuality): Bitmap {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, imageQuality.toBitmapQuality(), byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

// Extension functions
fun Bitmap.rescale(newWidth: Int, newHeight : Int,) : Bitmap {
    val width = this.width
    val height = this.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    val resizedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
    return resizedBitmap
}

val Bitmap.readableSize: String get() = byteCount.bytesToReadableSize()

fun Int.bytesToReadableSize(): String {
    // bytes, kb, mb, gb with comma
    val size = this.toFloat()
    val kb = size / 1024
    val mb = kb / 1024
    val gb = mb / 1024

    // add comma to number
    fun Float.addComma() = this.toString().split(".").let {
        val first = it[0].reversed().chunked(3).joinToString(",").reversed()
        val second = it.getOrNull(1) ?: ""
        "$first.$second"
    }
    return when {
        gb >= 1 -> "${gb.addComma()} GB"
        mb >= 1 -> "${mb.addComma()} MB"
        kb >= 1 -> "${kb.addComma()} KB"
        else -> "${size.addComma()} bytes"
    }
}

fun Long.bytesToReadableSize(): String = this.toInt().bytesToReadableSize()

fun Long.bytesToSizeAndUnit(): Pair<Float, String> {
    val size = this.toFloat()
    val decimalFormat = "%.2f"
    val kb = decimalFormat.format(size / 1024).toFloat()
    val mb = decimalFormat.format(kb / 1024).toFloat()
    val gb = decimalFormat.format(mb / 1024).toFloat()
    return when {
        gb >= 1 -> Pair(gb, "GB")
        mb >= 1 -> Pair(mb, "MB")
        kb >= 1 -> Pair(kb, "KB")
        else -> Pair(size, "B")
    }
}