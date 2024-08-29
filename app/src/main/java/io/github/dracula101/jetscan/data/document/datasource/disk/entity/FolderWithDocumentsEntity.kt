package io.github.dracula101.jetscan.data.document.datasource.disk.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FolderWithDocumentsEntity (
    @Embedded
    val folderEntity: DocumentFolderEntity,
    @Relation(
        parentColumn = DocumentFolderEntity.Columns.id,
        entityColumn = DocumentEntity.Columns.folderId,
        entity = DocumentEntity::class,
        projection = [
            DocumentEntity.Columns.id,
            DocumentEntity.Columns.uid,
            DocumentEntity.Columns.name,
            DocumentEntity.Columns.uri,
            DocumentEntity.Columns.size,
            DocumentEntity.Columns.dateCreated,
            DocumentEntity.Columns.dateModified,
            DocumentEntity.Columns.previewImageUri,
            DocumentEntity.Columns.folderId
        ]
    )
    val documentEntities: List<DocumentEntity>
)