package io.github.dracula101.jetscan.data.document.datasource.network.repository

import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfCompressResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfCompressSizesResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfMergeResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfOcrResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfSplitResult
import java.io.File

interface PdfToolRepository {

    suspend fun mergePdfFiles(
        files: List<File>,
        fileName: String,
        outputFile: File
    ) : PdfMergeResult


    suspend fun splitPdfFile(
        file: File,
        outputFiles: List<File>,
        ranges: String,
    ) : PdfSplitResult


    suspend fun compressPdfFile(
        file: File,
        quality: Int,
        outputFile: File
    ) : PdfCompressResult

    suspend fun getPdfCompressionSizes(
        file: File,
        qualities: List<Int>
    ) : PdfCompressSizesResult

    suspend fun getOcrPdf(
        file: File
    ) : PdfOcrResult
}