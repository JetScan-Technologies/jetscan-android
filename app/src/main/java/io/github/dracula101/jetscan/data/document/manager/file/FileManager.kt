package io.github.dracula101.jetscan.data.document.manager.file

import android.graphics.Bitmap
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import java.io.File

interface FileManager {
    // ================ Imported Documents ================
    fun getImportDocuments(): List<File>

    fun getImportDocument(name: String): File?

    fun uploadImportDocument(file: File): Boolean


    fun uploadImportDocumentFile(file: File): File?

    fun uploadImportDocument(document: Document): Boolean

    fun deleteImportDocument(file: File)

    fun deleteImportDocuments()

    // ================ Scanned Documents ================
    suspend fun addScannedDocument(
        uri: Uri,
        imageQuality: ImageQuality = ImageQuality.MEDIUM,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit
    ): Task<ScannedDocDirectory>

    suspend fun addScannedDocumentFromScanner(
        bitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int = 90,
        delayDuration: Long = 50L,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): Task<ScannedDocDirectory>

    fun savePreviewImageUri(uri: Uri, mainDirectory: File, isPdf: Boolean): Task<File>

    fun deleteScannedDocument(fileName: String): Boolean
}