package io.github.dracula101.jetscan.data.document.datasource.disk.converters

import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentFolderEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.FolderWithDocumentsEntity
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder

fun FolderWithDocumentsEntity.toDocumentFolder() = DocumentFolder(
    id = folderEntity.uid,
    name = folderEntity.name,
    dateCreated = folderEntity.dateCreated,
    dateModified = folderEntity.dateModified,
    documentCount = documentEntities.size,
    documents = documentEntities.map { it.toDocument() },
    path = folderEntity.path
)

fun DocumentFolder.toDocumentFolderEntity() = DocumentFolderEntity(
    id = 0,
    uid = id,
    name = name,
    dateCreated = dateCreated,
    dateModified = dateModified,
    documentCount = documentCount,
    path = path,
    pathDepth = path.count { it == '/' },
    parentPath = path.substringBeforeLast('/')
)
