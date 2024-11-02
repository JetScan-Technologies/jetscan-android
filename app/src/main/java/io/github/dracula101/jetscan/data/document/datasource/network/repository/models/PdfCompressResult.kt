package io.github.dracula101.jetscan.data.document.datasource.network.repository.models

import java.io.File

sealed class PdfCompressResult {

    data class Success(val file: File) : PdfCompressResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : PdfCompressResult()
}