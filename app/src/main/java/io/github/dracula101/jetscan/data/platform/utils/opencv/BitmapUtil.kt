package io.github.dracula101.jetscan.data.platform.utils.opencv

import android.graphics.Bitmap
import org.opencv.core.Mat

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