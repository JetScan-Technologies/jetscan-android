package io.github.dracula101.jetscan.data.document.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocument
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentFolder
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentFolderEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toScannedImageEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentFolderDao
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerErrorType
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.data.document.repository.models.DocumentErrorType
import io.github.dracula101.jetscan.data.document.repository.models.DocumentResult
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.pdf.models.PdfOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val folderDocumentDao: DocumentFolderDao,
    private val documentManager: DocumentManager,
) : DocumentRepository {
    override fun getDocuments(excludeFolders: Boolean): Flow<List<Document>?> {
        return if (excludeFolders) {
            documentDao.getAllDocuments().map {
                it.map { documentWithImages ->
                    documentWithImages.toDocument()
                }
            }
        } else {
            documentDao.getRecentDocuments().map {
                it.map { documentWithImages ->
                    documentWithImages.toDocument()
                }
            }
        }
    }

    override fun getDocument(id: Long): Flow<Document?> {
        return documentDao.getDocumentById(id).map {
            it?.toDocument()
        }
    }

    override fun getDocumentByUid(uid: String): Flow<Document?> {
        return documentDao.getDocumentByUid(uid).map {
            it?.toDocument()
        }
    }

    override fun getDocument(name: String): Flow<Document?> {
        return documentDao.getDocumentByName(name).map {
            it?.toDocument()
        }
    }

    override fun getFolders(path: String): Flow<List<DocumentFolder>?> {

        return folderDocumentDao.getAllFolders(
            path = path,
            pathDepth = path.count { it == '/' }
        ).map {
            it.map { folderWithDocuments ->
                folderWithDocuments.toDocumentFolder()
            }
        }
    }

    override fun getInternalFolders(rootPath: String, pathDepth: Int): Flow<List<DocumentFolder>?> {
        return folderDocumentDao.getInternalFolders(
            rootPath = rootPath,
            pathDepth = pathDepth,
        ).map {
            it?.map { folderWithDocuments ->
                folderWithDocuments.toDocumentFolder()
            } ?: emptyList()
        }
    }

    override fun getFolderByUid(uid: String): Flow<DocumentFolder?> {
        return folderDocumentDao.getFolderByUid(uid).map {
            it?.toDocumentFolder()
        }
    }

    override fun getFolderByName(name: String): Flow<DocumentFolder?> {
        return folderDocumentDao.getFolderByName(name).map {
            it?.toDocumentFolder()
        }
    }

    override suspend fun isDocumentExists(name: String): Boolean {
        return try {
            documentDao.isDocumentExists(name)
        } catch (e: Exception) { false }
    }

    override suspend fun addImportDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): DocumentResult<Document> {
        return try {
            val documentExists = documentDao.isDocumentExists(fileName)
            if (documentExists) {
                return DocumentResult.Error(
                    message = "Document already exists",
                    type = DocumentErrorType.DUPLICATE_DOCUMENT
                )
            }
            val timeCreated = System.currentTimeMillis()
            val result = documentManager.addDocument(
                imageQuality =  imageQuality,
                uri = uri,
                fileName = fileName,
                progressListener = progressListener
            )
            delay(500)
            return when(result){
                is DocManagerResult.Success -> {
                    val document = Document(
                        name = fileName,
                        dateCreated = timeCreated,
                        dateModified = System.currentTimeMillis(),
                        size = documentManager.getFileLength(uri),
                        uri = result.data.originalFile.toUri(),
                        previewImageUri = result.data.previewFile?.toUri(),
                        mimeType = documentManager.getMimeType(uri),
                        extension = documentManager.getExtension(uri),
                    )
                    val documentId = documentDao.insertDocument(document.toDocumentEntity())
                    val scannedImages = result.data.imageDir.listFiles()?.map { ScannedImage.fromFile(it) }
                        ?: emptyList()
                    documentDao.insertImages(scannedImages.map { it.toScannedImageEntity(documentId) })
                    DocumentResult.Success(document)
                }
                is DocManagerResult.Error -> {
                    DocumentResult.Error(
                        message = result.message,
                        error = result.error,
                        type = when(result.type) {
                            DocManagerErrorType.UNKNOWN -> DocumentErrorType.UNKNOWN
                            else-> DocumentErrorType.INVALID_DOCUMENT
                        }
                    )
                }
            }
        } catch (error: Exception) {
            Timber.e(error)
            DocumentResult.Error(
                message = when(error) {
                    is IllegalStateException -> "Invalid file"
                    is IOException -> "Failed to read file"
                    is SQLException -> "Database File Operation"
                    is SecurityException -> "Exceeded file size limit"
                    else -> "Failed to import document"
                },
                error = error,
                type = when(error){
                    is IllegalStateException -> DocumentErrorType.INVALID_DOCUMENT
                    is IOException -> DocumentErrorType.INVALID_DOCUMENT
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    is SecurityException -> DocumentErrorType.SECURITY_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun addDocumentFromScanner(
        originalBitmaps: List<Bitmap>,
        scannedBitmaps: List<Bitmap>,
        fileName: String,
        pdfOptions: PdfOptions,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit
    ): DocumentResult<Document> {
        return try {
            if (documentDao.isDocumentExists(fileName)) {
                return DocumentResult.Error(
                    message = "Document already exists",
                    type = DocumentErrorType.DUPLICATE_DOCUMENT
                )
            }
            val result = documentManager.addDocumentFromScanner(
                originalBitmaps = originalBitmaps,
                scannedBitmaps = scannedBitmaps,
                fileName = fileName,
                progressListener = progressListener,
                pdfOptions = pdfOptions
            )
            delay(500)
            return when(result){
                is DocManagerResult.Success -> {
                    val timeCreated = System.currentTimeMillis()
                    val document = Document(
                        name = fileName,
                        dateCreated = timeCreated,
                        dateModified = timeCreated,
                        size = result.data.originalFile.length(),
                        uri = result.data.originalFile.toUri(),
                        previewImageUri = result.data.previewFile?.toUri()
                    )
                    val documentId = documentDao.insertDocument(document.toDocumentEntity())
                    val scannedImages = result.data.scannedImageDir?.listFiles()?.map { ScannedImage.fromFile(it) }
                        ?: emptyList()
                    documentDao.insertImages(scannedImages.map { it.toScannedImageEntity(documentId) })
                    DocumentResult.Success(document)
                }
                is DocManagerResult.Error -> {
                    DocumentResult.Error(
                        message = result.message,
                        error = result.error,
                        type = when(result.type) {
                            DocManagerErrorType.UNKNOWN -> DocumentErrorType.UNKNOWN
                            else -> DocumentErrorType.INVALID_DOCUMENT
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e) {
                    is IllegalStateException -> "Invalid file"
                    is IOException -> "Failed to read file"
                    is SQLException -> "Database File Operation"
                    is SecurityException -> "Exceeded file size limit"
                    else -> "Failed to import document"
                },
                error = e,
                type = when(e){
                    is IllegalStateException -> DocumentErrorType.INVALID_DOCUMENT
                    is IOException -> DocumentErrorType.INVALID_DOCUMENT
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    is SecurityException -> DocumentErrorType.SECURITY_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun addFolder(folderName: String, path: String): DocumentResult<Long> {
        return try {
            val folder = DocumentFolder(name = folderName, dateCreated = System.currentTimeMillis(), documentCount = 0, documents = emptyList(), path = path)
            val folderId = folderDocumentDao.insertFolder(folder.toDocumentFolderEntity())
            DocumentResult.Success(folderId)
        } catch (error: Exception) {
            Timber.e(error)
            DocumentResult.Error(
                message = when(error){
                    is SQLException -> "Error in database operation"
                    is IllegalStateException -> "Invalid folder name"
                    is IOException -> "Failed to create folder"
                    else -> "Failed to create folder"
                },
                error = error,
                type = when(error){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    is IllegalStateException, is IOException -> DocumentErrorType.INVALID_FOLDER
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun addDocumentToFolder(document: Document, folder: DocumentFolder): DocumentResult<Long> {
        return try {
            val folderPrimaryId = folderDocumentDao.getFolderByUid(folder.id).firstOrNull()?.folderEntity?.id
                ?: return DocumentResult.Error(
                    message = "Folder not found",
                    type = DocumentErrorType.FOLDER_NOT_FOUND
                )
            documentDao.addDocumentToFolder(
                documentUid = document.id,
                folderId = folderPrimaryId
            )
            folderDocumentDao.updateFolder(folder.toDocumentFolderEntity().copy(dateModified = System.currentTimeMillis()))
            DocumentResult.Success(folderPrimaryId)
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to add document to folder"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun updateDocument(document: Document): DocumentResult<Nothing> {
        return try {
            documentDao.updateDocument(document.toDocumentEntity().copy(dateModified = System.currentTimeMillis()))
            DocumentResult.Success()
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to update document"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun updateDocumentImage(
        bitmap: Bitmap,
        documentUid: String,
        documentImageIndex: Int
    ): DocumentResult<Nothing> {
        return try {
            val document = documentDao.getDocumentByUid(documentUid).first()
                ?: return DocumentResult.Error(
                    message = "Document not found",
                    type = DocumentErrorType.DOCUMENT_NOT_FOUND
                )
            val documentResult = documentManager.updateDocumentImage(
                bitmap = bitmap,
                documentName = document.documentEntity.name,
                documentImageIndex = documentImageIndex
            )
            return when(documentResult){
                is DocManagerResult.Error -> {
                     DocumentResult.Error(
                        message = documentResult.message,
                        error = documentResult.error,
                        type = when(documentResult.type){
                            DocManagerErrorType.UNKNOWN -> DocumentErrorType.UNKNOWN
                            else -> DocumentErrorType.INVALID_DOCUMENT
                        }
                    )
                }
                is DocManagerResult.Success -> {
                    var scannedImageEntity = document.scannedImageEntities.getOrNull(documentImageIndex)
                        ?: return DocumentResult.Error(
                            message = "Scanned image not found",
                            type = DocumentErrorType.INVALID_DOCUMENT
                        )
                    scannedImageEntity = scannedImageEntity.copy(
                        width = bitmap.width,
                        height = bitmap.height,
                        size = documentResult.data.length(),
                        date = System.currentTimeMillis(),
                        uri = documentResult.data.toUri().toString(),
                    )
                    documentDao.updateDocumentImage(scannedImageEntity)
                    val documentEntity = document.documentEntity.copy(
                        dateModified = System.currentTimeMillis(),
                    )
                    documentDao.updateDocument(documentEntity)
                    DocumentResult.Success()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to update document image"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }


    override suspend fun updatePdfDocument(pdf: File, document: Document): DocumentResult<Nothing> {
        return try {
            val documentManagerResult = documentManager.replacePdf(
                documentName = document.name,
                tempPdf = pdf,
            )
            when(documentManagerResult){
                is DocManagerResult.Success -> {
                    val documentEntity = document.toDocumentEntity().copy(
                        dateModified = System.currentTimeMillis(),
                        size = documentManagerResult.data.length(),
                        uri = documentManagerResult.data.toUri().toString()
                    )
                    documentDao.updateDocument(documentEntity)
                    DocumentResult.Success()
                }
                is DocManagerResult.Error -> {
                    Timber.e(documentManagerResult.error)
                    DocumentResult.Error(
                        message = documentManagerResult.message,
                        error = documentManagerResult.error,
                        type = when(documentManagerResult.type){
                            DocManagerErrorType.UNKNOWN -> DocumentErrorType.UNKNOWN
                            else -> DocumentErrorType.INVALID_DOCUMENT
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to delete all documents"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun deleteAllDocuments(): DocumentResult<Nothing> {
        return try {
            documentDao.deleteAllDocuments()
            DocumentResult.Success()
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to delete all documents"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun deleteDocument(document: Document): DocumentResult<Nothing> {
        return try {
            when(val result = documentManager.deleteDocument(document.name)){
                is DocManagerResult.Success -> {
                    val documentPrimaryId = documentDao.getDocumentByUid(document.id).firstOrNull()?.documentEntity?.id
                        ?: return DocumentResult.Error(
                            message = "Document not found",
                            type = DocumentErrorType.DOCUMENT_NOT_FOUND
                        )
                    documentDao.deleteDocumentById(documentPrimaryId)
                    DocumentResult.Success()
                }
                is DocManagerResult.Error -> {
                    DocumentResult.Error(
                        message = result.message,
                        error = result.error,
                        type = when(result.type) {
                            DocManagerErrorType.UNKNOWN -> DocumentErrorType.UNKNOWN
                            else -> DocumentErrorType.INVALID_DOCUMENT
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to delete document"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun deleteFolder(folder: DocumentFolder): DocumentResult<Nothing> {
        return try {
            folder.documents.forEach { document ->
                documentDao.deleteFolderRefByDocUid(document.id)
            }
            folderDocumentDao.deleteFolderByUid(folder.id)
            DocumentResult.Success()
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to delete folder"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun deleteDocumentFromFolder(document: Document, folder: DocumentFolder): DocumentResult<Nothing> {
        return try {
            documentDao.deleteFolderRefByDocUid(document.id)
            val folderEntity = folderDocumentDao.getFolderByUid(folder.id).firstOrNull()?.folderEntity
                ?: return DocumentResult.Error(
                    message = "Folder not found",
                    type = DocumentErrorType.FOLDER_NOT_FOUND
                )
            folderDocumentDao.updateFolder(folderEntity.copy(dateModified = System.currentTimeMillis()))
            DocumentResult.Success()
        } catch (e: Exception) {
            Timber.e(e)
            DocumentResult.Error(
                message = when(e){
                    is SQLException -> "Error in database operation"
                    else -> "Failed to delete document from folder"
                },
                error = e,
                type = when(e){
                    is SQLException -> DocumentErrorType.DB_EXCEPTION
                    else -> DocumentErrorType.UNKNOWN
                }
            )
        }
    }
}