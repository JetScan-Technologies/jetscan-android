package io.github.dracula101.jetscan.data.document.repository

import android.graphics.Bitmap
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.data.document.repository.models.DocumentResult
import io.github.dracula101.pdf.models.PdfOptions
import kotlinx.coroutines.flow.Flow
import java.io.File


interface DocumentRepository {

    // ================= Get Documents =================
    fun getDocuments(excludeFolders: Boolean = true): Flow<List<Document>?>
    fun getDocument(id: Long): Flow<Document?>
    fun getDocumentByUid(uid: String): Flow<Document?>
    fun getDocument(name: String): Flow<Document?>

    // ================= Get Folder =================
    fun getFolders(path: String): Flow<List<DocumentFolder>?>
    fun getInternalFolders(rootPath:String, pathDepth:Int): Flow<List<DocumentFolder>?>
    fun getFolderByUid(uid: String): Flow<DocumentFolder?>
    fun getFolderByName(name: String): Flow<DocumentFolder?>

    // ================= Insert Documents =================
    suspend fun isDocumentExists(name: String): Boolean
    suspend fun addImportDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit = { _, _ -> }
    ): DocumentResult<Document>

    suspend fun addDocumentFromScanner(
        originalBitmaps: List<Bitmap>,
        scannedBitmaps: List<Bitmap>,
        fileName: String,
        pdfOptions: PdfOptions,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit = { _, _ -> }
    ): DocumentResult<Document>

    // ================= Insert Folder =================
    suspend fun addFolder(folderName: String, path: String): DocumentResult<Long>
    suspend fun addDocumentToFolder(document: Document, folder: DocumentFolder): DocumentResult<Long>

    // ================= Update Documents =================
    suspend fun updateDocument(document: Document): DocumentResult<Nothing>
    suspend fun updateDocumentImage(
        bitmap: Bitmap,
        documentUid: String,
        documentImageIndex: Int,
    ): DocumentResult<Nothing>
    suspend fun updatePdfDocument(
        pdf: File,
        document: Document
    ): DocumentResult<Nothing>

    // ================= Delete Documents =================
    suspend fun deleteAllDocuments(): DocumentResult<Nothing>
    suspend fun deleteDocument(document: Document): DocumentResult<Nothing>

    // ================= Delete Folder =================
    suspend fun deleteFolder(folder: DocumentFolder): DocumentResult<Nothing>
    suspend fun deleteDocumentFromFolder(document: Document, folder: DocumentFolder): DocumentResult<Nothing>

}