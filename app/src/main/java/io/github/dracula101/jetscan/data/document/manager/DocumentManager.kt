package io.github.dracula101.jetscan.data.document.manager

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import java.io.File

interface DocumentManager {

    val localDocumentFlow: Flow<List<DocumentDirectory>>

    suspend fun addDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit
    ): Task<DocumentDirectory>

    suspend fun addDocumentFromScanner(
        bitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int,
        delayDuration: Long = 50L,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): Task<DocumentDirectory>

    fun savePreviewImageUri(uri: Uri, mainDirectory: File, isPdf: Boolean): Task<File>

    fun deleteDocument(fileName: String): Boolean

    fun getBitmapFromUri(uri: Uri): Bitmap?

    fun getFileName(uri: Uri, withoutExtension: Boolean = false): String?

    fun getFileLength(uri: Uri): Long

    fun formatFileSize(file: File): String

    fun getReadableFileSize(length: Long): String?

    fun formatDate(time: Long): String

    fun getMimeType(uri: Uri): MimeType

    fun getExtension(uri: Uri): Extension

}

@Parcelize
data class DocumentDirectory(
    val mainDirectory: File,
    val imageDirectory: File,
    val originalFile: File?,
    val previewDirectory: File?,
) : Parcelable
