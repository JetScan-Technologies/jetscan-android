package io.github.dracula101.jetscan.data.document.manager.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import java.io.File

interface ImageManager {

    fun saveImageFromUri(contentResolver: ContentResolver, uri: Uri, name: String, path: String?): File?

    fun saveImageFromFile(file: File, bitmap: Bitmap, quality: ImageQuality): Boolean

    fun compress(bitmap: Bitmap, quality: ImageQuality): Bitmap

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap

}