package io.github.dracula101.jetscan.data.document.models.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import java.io.File
import java.text.DecimalFormat
import kotlin.math.pow


fun ScannedImage.fromBitmap(
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

fun ScannedImage.fromFile(file: File): ScannedImage {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    return ScannedImage(
        size = file.length(),
        scannedUri = file.toUri(),
        date = System.currentTimeMillis(),
        width = bitmap.width,
        height = bitmap.height
    )
}


private fun getReadableSizeUnit(digitGroups: Int): String {
    return when (digitGroups) {
        0 -> "B"
        1 -> "KB"
        2 -> "MB"
        3 -> "GB"
        4 -> "TB"
        5 -> "PB"
        6 -> "EB"
        7 -> "ZB"
        8 -> "YB"
        else -> "Unknown"
    }
}

fun ScannedImage.getReadableFileSize(): String {
    val size = size.toDouble()
    val sizeUnit = 1000
    if (size <= 0) {
        return "0"
    }
    val digitGroups =
        (kotlin.math.log10(size) / kotlin.math.log10(sizeUnit.toDouble())).toInt()
    return DecimalFormat("#,##0.##").format(
        size / sizeUnit.toDouble().pow(digitGroups.toDouble())
    ) + " " + getReadableSizeUnit(digitGroups)
}

