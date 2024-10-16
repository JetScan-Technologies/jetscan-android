package io.github.dracula101.jetscan.data.ocr.repository

import io.github.dracula101.jetscan.data.ocr.repository.models.OcrDocumentResult
import io.github.dracula101.jetscan.data.ocr.repository.models.ocr.OcrResult
import java.io.File

interface OcrRepository {

    suspend fun processImageDocument(
        file: File,
        mimeType: String
    ) : OcrDocumentResult<OcrResult>

}