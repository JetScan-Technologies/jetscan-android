package io.github.dracula101.jetscan.data.ocr.repository.models

sealed class OcrDocumentResult<out T> {

    data class Success<T>(
        val data: T
    ) : OcrDocumentResult<T>()

    data class Error(
        val message: String,
        val exception: Exception
    ) : OcrDocumentResult<Nothing>()

}