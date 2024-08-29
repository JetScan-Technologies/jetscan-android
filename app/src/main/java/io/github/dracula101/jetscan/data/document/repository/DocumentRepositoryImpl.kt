package io.github.dracula101.jetscan.data.document.repository

import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocument
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentFolder
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocumentFolderEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toScannedImageEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentFolderDao
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val folderDocumentDao: DocumentFolderDao
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
        return documentDao.isDocumentExists(name)
    }

    override suspend fun addDocument(document: Document): Boolean {
        return try {
            val documentId = documentDao.insertDocument(document.toDocumentEntity())
            documentDao.insertImages(document.scannedImages.map { it.toScannedImageEntity(documentId) })
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun addDocumentFile(file: File): Document? {
        return null
    }

    override suspend fun addFolder(folderName: String, path: String): Boolean {
        return try {
            val folder = DocumentFolder(name = folderName, dateCreated = System.currentTimeMillis(), documentCount = 0, documents = emptyList(), path = path)
            folderDocumentDao.insertFolder(folder.toDocumentFolderEntity())
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun addDocumentToFolder(document: Document, folder: DocumentFolder): Boolean {
        return try {
            val folderPrimaryId = folderDocumentDao.getFolderByUid(folder.id).firstOrNull()?.folderEntity?.id
                ?: return false
            documentDao.addDocumentToFolder(
                documentUid = document.id,
                folderId = folderPrimaryId
            )
            folderDocumentDao.updateFolder(folder.toDocumentFolderEntity().copy(dateModified = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun insertDocuments(documents: List<Document>): Boolean {
        return try {
            documentDao.insertDocuments(documents.map { it.toDocumentEntity() })
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun updateDocument(document: Document): Boolean {
        return try {
            documentDao.updateDocument(document.toDocumentEntity().copy(dateModified = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun deleteAllDocuments(): Boolean {
        return try {
            documentDao.deleteAllDocuments()
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun deleteDocument(document: Document): Boolean {
        return try {
            val documentPrimaryId = documentDao.getDocumentByUid(document.id).firstOrNull()?.documentEntity?.id
                ?: return false
            documentDao.deleteDocumentById(documentPrimaryId)
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun deleteFolder(folder: DocumentFolder): Boolean {
        return try {
            folder.documents.forEach { document ->
                documentDao.deleteFolderRefByDocUid(document.id)
            }
            folderDocumentDao.deleteFolderByUid(folder.id)
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun deleteDocumentFromFolder(document: Document, folder: DocumentFolder): Boolean {
        return try {
            documentDao.deleteFolderRefByDocUid(document.id)
            val folderEntity = folderDocumentDao.getFolderByUid(folder.id).firstOrNull()?.folderEntity
                ?: return false
            folderDocumentDao.updateFolder(folderEntity.copy(dateModified = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}