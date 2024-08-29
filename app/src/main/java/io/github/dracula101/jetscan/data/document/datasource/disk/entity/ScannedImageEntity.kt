package io.github.dracula101.jetscan.data.document.datasource.disk.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = ScannedImageEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = [DocumentEntity.Columns.id],
            childColumns = [ScannedImageEntity.Columns.documentId],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        androidx.room.Index(value = [ScannedImageEntity.Columns.documentId])
    ],
)
data class ScannedImageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(Columns.id) val id: Long,
    @ColumnInfo(name = Columns.documentId) val documentId: Long,
    @ColumnInfo(name = Columns.width) val width: Int,
    @ColumnInfo(name = Columns.height) val height: Int,
    @ColumnInfo(name = Columns.size) val size: Long,
    @ColumnInfo(name = Columns.date) val date: Long,
    @ColumnInfo(name = Columns.uri) val uri: String,
    @ColumnInfo(name = Columns.quality) val quality: String,
){
    companion object{
        const val TABLE_NAME = "scanned_images"
    }

    object Columns{
        const val id: String = "id"
        const val documentId: String = "document_id"
        const val width: String = "width"
        const val height: String = "height"
        const val size: String = "size"
        const val date: String = "date"
        const val uri: String = "uri"
        const val quality: String = "quality"
    }
}