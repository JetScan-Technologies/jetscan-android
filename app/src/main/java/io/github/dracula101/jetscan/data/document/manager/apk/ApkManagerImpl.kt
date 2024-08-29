package io.github.dracula101.jetscan.data.document.manager.apk


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import okio.use
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ApkManagerImpl : ApkManager {

    override fun getApkIcon(context: Context, apkFile: File): Drawable? {
        val packageManager = context.packageManager
        val packageInfo =
            packageManager.getPackageArchiveInfo(apkFile.path, PackageManager.GET_ACTIVITIES)
        if (packageInfo != null) {
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = apkFile.path
            applicationInfo.publicSourceDir = apkFile.path
            return applicationInfo.loadIcon(packageManager)
        }
        return null
    }

    override fun saveApkIcon(context: Context, uri: Uri, name: String): File? {
        try {
            val apkIconDrawable = loadApkIcon(context, uri)
            if (apkIconDrawable != null) {
                val bitmap = apkIconDrawable.toBitmap()
                val file = File(context.filesDir, name.substringBeforeLast(".").plus(".png"))
                val outputStream = FileOutputStream(file)
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    ImageQuality.HIGH.toBitmapQuality(),
                    outputStream
                )
                outputStream.flush()
                return file
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    private fun loadApkIcon(context: Context, uri: Uri): Drawable? {
        val file = File(context.cacheDir, "temp.apk")
        if (file.exists()) {
            file.delete()
        }
        context.contentResolver.openInputStream(uri).use {
            val outputStream = FileOutputStream(file)
            it?.copyTo(outputStream)
            it?.close()
            Timber.i("File saved to ${file.path}, File Length ${file.length()}")
            outputStream.flush()
            outputStream.close()
        }
        val packageManager = context.packageManager
        val packageInfo =
            packageManager.getPackageArchiveInfo(file.path, PackageManager.GET_ACTIVITIES)
        if (packageInfo != null) {
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = file.path
            applicationInfo.publicSourceDir = file.path
            return applicationInfo.loadIcon(packageManager).run {
                if (this is Drawable) this
                else {
                    Timber.e("Drawable is null")
                    null
                }
            }
        }
        return null
    }

}