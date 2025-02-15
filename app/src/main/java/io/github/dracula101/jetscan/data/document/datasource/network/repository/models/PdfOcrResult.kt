package io.github.dracula101.jetscan.data.document.datasource.network.repository.models

sealed class PdfOcrResult {

    data class Success(
        val data: OcrResult
    ) : PdfOcrResult()

    data class Error(
        val message: String,
        val exception: Exception
    ) : PdfOcrResult()

}