package io.github.dracula101.jetscan.data.document.datasource.network.repository

import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfMergeResult
import java.io.File

interface PdfToolRepository {

    suspend fun mergePdfFiles(
        files: List<File>,
        fileName: String,
        outputFile: File
    ) : PdfMergeResult

}