package io.github.dracula101.jetscan.data.document.datasource.network.repository.models

import io.github.dracula101.pdf.models.PdfCompressionLevel

sealed class PdfCompressSizesResult {

    data class Success(val compressSizes: Map<PdfCompressionLevel, Long>) : PdfCompressSizesResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : PdfCompressSizesResult()

}