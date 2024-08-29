package io.github.dracula101.jetscan.data.document.datasource.disk.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = DocumentEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = DocumentFolderEntity::class,
            parentColumns = [DocumentFolderEntity.Columns.id],
            childColumns = [DocumentEntity.Columns.folderId],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        androidx.room.Index(value = [DocumentEntity.Columns.folderId])
    ],
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.id) val id: Long,
    @ColumnInfo(name = Columns.uid) val uid: String,
    @ColumnInfo(name = Columns.name) val name: String,
    @ColumnInfo(name = Columns.uri) val uri: String,
    @ColumnInfo(name = Columns.size) val size: Long,
    @ColumnInfo(name = Columns.dateCreated) val dateCreated: Long,
    @ColumnInfo(name = Columns.dateModified) val dateModified: Long? = null,
    @ColumnInfo(name = Columns.previewImageUri) val previewImageUri: String? = null,
    @ColumnInfo(name = Columns.folderId) val folderId: Long? = null
){
    object Columns {
        const val id: String = "id"
        const val uid: String = "uid"
        const val name: String = "name"
        const val uri: String = "uri"
        const val size: String = "size"
        const val dateCreated: String = "date_created"
        const val dateModified: String = "date_modified"
        const val previewImageUri: String = "preview_image_uri"
        const val folderId: String = "folder_id"
    }

    companion object{
        const val TABLE_NAME = "documents"
    }
}

