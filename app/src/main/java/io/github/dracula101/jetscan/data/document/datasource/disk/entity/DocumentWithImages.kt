package io.github.dracula101.jetscan.data.document.datasource.disk.entity

import androidx.room.Embedded
import androidx.room.Relation


data class DocumentWithImages(
    @Embedded
    val documentEntity: DocumentEntity,
    @Relation(
        parentColumn = DocumentEntity.Columns.id,
        entityColumn = ScannedImageEntity.Columns.documentId,
        entity = ScannedImageEntity::class,
        projection = [
            ScannedImageEntity.Columns.id,
            ScannedImageEntity.Columns.documentId,
            ScannedImageEntity.Columns.width,
            ScannedImageEntity.Columns.height,
            ScannedImageEntity.Columns.size,
            ScannedImageEntity.Columns.date,
            ScannedImageEntity.Columns.uri,
            ScannedImageEntity.Columns.quality
        ]
    )
    val scannedImageEntities: List<ScannedImageEntity>
)