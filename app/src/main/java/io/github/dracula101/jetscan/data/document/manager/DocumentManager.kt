package io.github.dracula101.jetscan.data.document.manager

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import java.io.File

interface DocumentManager {

    suspend fun addDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit
    ): DocManagerResult<DocumentDirectory>

    suspend fun addDocumentFromScanner(
        originalBitmaps: List<Bitmap>,
        scannedBitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int,
        delayDuration: Long = 50L,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): DocManagerResult<DocumentDirectory>

    suspend fun addExtraDocument(
        file: File,
        fileName: String,
    ) : DocManagerResult<File?>

    suspend fun deleteExtraDocument(uri: Uri) : Boolean

    fun deleteDocument(fileName: String): DocManagerResult<Boolean>

    fun getBitmapFromUri(uri: Uri): Bitmap?

    fun getFileName(uri: Uri, withoutExtension: Boolean = false): String?

    fun getFileLength(uri: Uri): Long

    fun getMimeType(uri: Uri): MimeType

    fun getExtension(uri: Uri): Extension

}

@Parcelize
data class DocumentDirectory(
    val mainDir: File,
    val imageDir: File,
    val scannedImageDir: File?,
    val originalFile: File,
    val previewFile: File?,
) : Parcelable
