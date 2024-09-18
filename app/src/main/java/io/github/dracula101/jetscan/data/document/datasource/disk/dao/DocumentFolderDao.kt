package io.github.dracula101.jetscan.data.document.datasource.disk.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentFolderEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.FolderWithDocumentsEntity
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import kotlinx.coroutines.flow.Flow


/**
 *  Data Access Object for DocumentFolderEntity
 *  This class manages the documents that are grouped into folders.
 *  The [DocumentFolderEntity] class is the entity class for the document_folders table in the database.
 */
@Dao
interface DocumentFolderDao {

    /**
     * Inserts a new folder into the database.
     * @param folder The folder to be inserted.
     * @return the primary id of the folder inserted from the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: DocumentFolderEntity) : Long

    /**
     * Updates the multiple document folderId.
     * @param folderId The folder id to be updated in the document.
     * @param documentIds The list of document ids to be updated.
     * @return the number of rows updated in the database.
     */
    @Transaction
    @Query("UPDATE ${DocumentEntity.TABLE_NAME} SET ${DocumentEntity.Columns.folderId} = :folderId WHERE id IN (:documentIds)")
    suspend fun addDocumentToFolder(documentIds: List<Long>, folderId: Long): Int

    /**
     * Get all folders .
     * @return a list of all folders in the database.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${DocumentFolderEntity.Columns.path} = :path AND ${DocumentFolderEntity.Columns.pathDepth} = :pathDepth ORDER BY ${DocumentFolderEntity.Columns.dateCreated} DESC, ${DocumentFolderEntity.Columns.dateModified} DESC")
    fun getAllFolders(path: String = DocumentFolder.ROOT_FOLDER, pathDepth: Int = 1): Flow<List<FolderWithDocumentsEntity>>

    @Transaction
    @Query("SELECT * FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${(DocumentFolderEntity.Columns.parentPath)} = :rootPath AND ${DocumentFolderEntity.Columns.pathDepth} = :pathDepth ORDER BY ${DocumentFolderEntity.Columns.dateCreated} DESC, ${DocumentFolderEntity.Columns.dateModified} DESC")
    fun getInternalFolders(rootPath: String,pathDepth: Int = 1): Flow<List<FolderWithDocumentsEntity>?>

    /**
     * Get a folder by its uid.
     * @param uid The folder uid to be retrieved.
     * @return the folder with its documents.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${DocumentFolderEntity.Columns.uid} = :uid")
    fun getFolderByUid(uid: String): Flow<FolderWithDocumentsEntity?>

    /**
     * Get a folder by its name.
     * @param folderName The folder name to be retrieved.
     * @return the folder with its documents.
     */
    @Transaction
    @Query("SELECT * FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${DocumentFolderEntity.Columns.name} = :folderName")
    fun getFolderByName(folderName: String): Flow<FolderWithDocumentsEntity?>

    /**
     * Updates a folder in the database.
     * @param folder The folder to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE, entity = DocumentFolderEntity::class)
    suspend fun updateFolder(folder: DocumentFolderEntity)


    /**
     * Deletes a folder from the database.
     * @param folderId The folder primary id to be deleted.
     */
    @Query("DELETE FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${DocumentFolderEntity.Columns.id} = :folderId")
    suspend fun deleteFolder(folderId: Long)

    @Transaction
    @Query("DELETE FROM ${DocumentFolderEntity.TABLE_NAME} WHERE ${DocumentFolderEntity.Columns.uid} = :uid")
    suspend fun deleteFolderByUid(uid: String)

}