package io.github.dracula101.jetscan.data.document.datasource.disk.converters

import android.net.Uri
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.ScannedImageEntity
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage

fun ScannedImageEntity.toScannedImage() = ScannedImage(
    width = width,
    height = height,
    size = size,
    scannedUri = Uri.parse(uri),
    date = date,
    imageQuality = ImageQuality.valueOf(quality)
)

fun ScannedImage.toScannedImageEntity(documentId: Long) = ScannedImageEntity(
    id = 0,
    documentId = documentId,
    width = width,
    height = height,
    size = size,
    date = date,
    uri = scannedUri.toString(),
    quality = imageQuality.name,
)