package io.github.dracula101.jetscan.data.document.datasource.network.repository.models

import java.io.File

sealed class PdfSplitResult {
    data class Success(val files: List<File>) : PdfSplitResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : PdfSplitResult()

}