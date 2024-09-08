package io.github.dracula101.jetscan.data.document.manager

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.Dp
import io.github.dracula101.jetscan.data.document.models.doc.Document
import java.io.File

interface DocumentManager {

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap?

    fun fromUri(context: Context, uri: Uri) : Document

    fun fromFile(file: File, applicationContext: Context): Document

    fun getFileName(contentResolver: ContentResolver, uri: Uri, withoutExtension: Boolean = false): String?

    fun getFileLength(contentResolver: ContentResolver, uri: Uri): Long

    fun formatFileSize(contentResolver: ContentResolver, file: File): String

    fun loadPdfFirstPage(context: Context, file: File, size: Dp, density: Float): Bitmap?

    fun getReadableFileSize(length: Long): String?

    fun formatDate(time: Long): String

    fun getDocumentName(): String
}