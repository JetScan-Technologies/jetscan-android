package io.github.dracula101.jetscan.data.document.datasource.network.repository.models

import java.io.File

sealed class PdfMergeResult {

    data class Success(val file: File) : PdfMergeResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : PdfMergeResult()

}