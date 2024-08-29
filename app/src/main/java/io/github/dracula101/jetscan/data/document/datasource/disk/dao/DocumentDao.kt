package io.github.dracula101.jetscan.data.document.datasource.disk.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentWithImages
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.ScannedImageEntity
import kotlinx.coroutines.flow.Flow

/**
 *  Data Access Object for the DocumentEntity class.
 *  This class represents the database operations for the DocumentEntity class.
 *
 *  The [DocumentEntity] class is the entity class for the document table in the database.
 *  The [ScannedImageEntity] class is the entity class for the scanned_images table in the database.
 *
 *  The [DocumentWithImages] class is a data class that combines the [DocumentEntity] and [ScannedImageEntity] classes.
 *  This class is used to represent the relationship between the document and scanned_images tables in the database.
 */
@Dao
interface DocumentDao {

    /**
     * Inserts a document into the database.
     * @param document the document to be inserted.
     * @return the primary id of the document inserted from the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = DocumentEntity::class)
    suspend fun insertDocument(document: DocumentEntity): Long

    /**
     * Inserts a list of documents into the database.
     * @param documents the list of documents to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = DocumentEntity::class)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    /**
     * Inserts a list of images into the database.
     * @param scannedImages the list of images to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ScannedImageEntity::class)
    suspend fun insertImages(scannedImages: List<ScannedImageEntity>)

    @Transaction
    @Query("SELECT EXISTS(SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.name} LIKE :name)")
    suspend fun isDocumentExists(name: String): Boolean

    /**
     * Retrieves all documents from the database.
     * @return a list of all documents in the database.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.folderId} IS NULL ORDER BY ${DocumentEntity.Columns.dateCreated} DESC")
    fun getAllDocuments(): Flow<List<DocumentWithImages>>

    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} ORDER BY ${DocumentEntity.Columns.dateCreated} DESC")
    fun getRecentDocuments(): Flow<List<DocumentWithImages>>

    /**
     * Retrieves a document by its id from the database.
     * @param id the primary id from the document to be retrieved.
     * @return the document with the specified id.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.id} = :id")
    fun getDocumentById(id: Long): Flow<DocumentWithImages?>

    /**
     * Retrieves a document by its uid from the database.
     * @param uid the primary id from the document to be retrieved.
     * @return the document with the specified uid.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.uid} = :uid")
    fun getDocumentByUid(uid: String): Flow<DocumentWithImages?>


    /**
     * Retrieves a document by its name from the database.
     * @param name the name of the document to be retrieved.
     * @return the document with the specified name.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.folderId} LIKE :name")
    fun getDocumentByName(name: String): Flow<DocumentWithImages?>

    @Transaction
    @Query("SELECT * FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.name} LIKE :name")
    fun getDocumentIdByName(name: String): Long

    @Transaction
    @Query("UPDATE ${DocumentEntity.TABLE_NAME} SET ${DocumentEntity.Columns.folderId} = :folderId WHERE ${DocumentEntity.Columns.uid} = :documentUid")
    suspend fun addDocumentToFolder(
        documentUid: String,
        folderId: Long
    ): Int

    /**
     * Updates a document in the database.
     * @param documentEntity the document to be updated.
     */
    @Update(entity = DocumentEntity::class)
    suspend fun updateDocument(documentEntity: DocumentEntity)

    /**
     * Deletes a document by its id from the database.
     * @param id the primary id of the document to be deleted.
     */
    @Query("DELETE FROM ${DocumentEntity.TABLE_NAME} WHERE ${DocumentEntity.Columns.id} = :id")
    suspend fun deleteDocumentById(id: Long)

    /**
     * Deletes a document by its uid from the database.
     * @param uid the primary id of the document to be deleted.
     */
    @Query("UPDATE ${DocumentEntity.TABLE_NAME} SET ${DocumentEntity.Columns.folderId} = NULL WHERE ${DocumentEntity.Columns.uid} = :uid")
    suspend fun deleteFolderRefByDocUid(uid: String)
    /**
     * Deletes all documents from the database.
     */
    @Query("DELETE FROM ${DocumentEntity.TABLE_NAME}")
    suspend fun deleteAllDocuments()
}
