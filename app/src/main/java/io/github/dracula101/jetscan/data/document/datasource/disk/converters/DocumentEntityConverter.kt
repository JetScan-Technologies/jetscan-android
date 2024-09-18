package io.github.dracula101.jetscan.data.document.datasource.disk.converters

import android.net.Uri
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentWithImages
import io.github.dracula101.jetscan.data.document.models.doc.Document

fun DocumentEntity.toDocument() = Document(
    id = uid,
    name = name,
    uri = Uri.parse(uri),
    size = size,
    dateCreated = dateCreated,
    dateModified = dateModified,
    previewImageUri = previewImageUri?.let { Uri.parse(it) },
)

fun Document.toDocumentEntity() = DocumentEntity(
    id = 0,
    uid = id,
    name = name,
    uri = uri.toString(),
    size = size,
    dateCreated = dateCreated,
    dateModified = dateModified,
    previewImageUri = previewImageUri.toString(),
)

fun DocumentWithImages.toDocument() = Document(
    id = documentEntity.uid,
    name = documentEntity.name,
    uri = Uri.parse(documentEntity.uri),
    size = documentEntity.size,
    dateCreated = documentEntity.dateCreated,
    dateModified = documentEntity.dateModified,
    previewImageUri = documentEntity.previewImageUri?.let { Uri.parse(it) },
    scannedImages = scannedImageEntities.map { it.toScannedImage() }
)


