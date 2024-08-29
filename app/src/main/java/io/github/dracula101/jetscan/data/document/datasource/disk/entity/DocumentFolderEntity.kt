package io.github.dracula101.jetscan.data.document.datasource.disk.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = DocumentFolderEntity.TABLE_NAME,
)
data class DocumentFolderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Columns.id) val id: Long,
    @ColumnInfo(name = Columns.uid) val uid: String,
    @ColumnInfo(name = Columns.name) val name: String,
    @ColumnInfo(name = Columns.dateCreated) val dateCreated: Long,
    @ColumnInfo(name = Columns.dateModified) val dateModified: Long? = null,
    @ColumnInfo(name = Columns.documentCount) val documentCount: Int,
    @ColumnInfo(name = Columns.path) val path: String,
    @ColumnInfo(name = Columns.parentPath) val parentPath: String,
    @ColumnInfo(name = Columns.pathDepth) val pathDepth: Int
) {
    companion object {
        const val TABLE_NAME = "folders"
    }

    object Columns {
        const val id: String = "id"
        const val uid: String = "uid"
        const val name: String = "name"
        const val dateCreated: String = "date_created"
        const val dateModified: String = "date_modified"
        const val documentCount: String = "document_count"
        const val path: String = "path"
        const val parentPath: String = "parent_path"
        const val pathDepth: String = "path_depth"
    }
}