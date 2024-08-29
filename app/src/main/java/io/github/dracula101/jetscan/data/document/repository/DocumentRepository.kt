package io.github.dracula101.jetscan.data.document.repository

import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import kotlinx.coroutines.flow.Flow
import java.io.File


interface DocumentRepository {

    // ================= Get Documents =================
    fun getDocuments(excludeFolders: Boolean = true): Flow<List<Document>?>
    fun getDocument(id: Long): Flow<Document?>
    fun getDocument(name: String): Flow<Document?>

    // ================= Get Folder =================
    fun getFolders(path: String): Flow<List<DocumentFolder>?>
    fun getInternalFolders(rootPath:String, pathDepth:Int): Flow<List<DocumentFolder>?>
    fun getFolderByUid(uid: String): Flow<DocumentFolder?>
    fun getFolderByName(name: String): Flow<DocumentFolder?>

    // ================= Insert Documents =================
    suspend fun isDocumentExists(name: String): Boolean
    suspend fun addDocument(document: Document): Boolean
    suspend fun addDocumentFile(file: File): Document?
    suspend fun insertDocuments(documents: List<Document>): Boolean

    // ================= Insert Folder =================
    suspend fun addFolder(folderName: String, path: String): Boolean
    suspend fun addDocumentToFolder(document: Document, folder: DocumentFolder): Boolean

    // ================= Update Documents =================
    suspend fun updateDocument(document: Document): Boolean

    // ================= Delete Documents =================
    suspend fun deleteAllDocuments(): Boolean
    suspend fun deleteDocument(document: Document): Boolean

    // ================= Delete Folder =================
    suspend fun deleteFolder(folder: DocumentFolder): Boolean
    suspend fun deleteDocumentFromFolder(document: Document, folder: DocumentFolder): Boolean

}