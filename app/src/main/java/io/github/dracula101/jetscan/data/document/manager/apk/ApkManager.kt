package io.github.dracula101.jetscan.data.document.manager.apk

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.File

interface ApkManager {

    fun saveApkIcon(context: Context, uri: Uri, name: String): File?
    fun getApkIcon(context: Context, apkFile: File): Drawable?
}