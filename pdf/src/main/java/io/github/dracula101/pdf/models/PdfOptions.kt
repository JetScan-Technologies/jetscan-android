package io.github.dracula101.pdf.models

data class PdfOptions(
    val pageSize: PdfPageSize = PdfPageSize.A4,
    val hasMargin: Boolean = false,
    val quality: PdfQuality,
    val imageQuality: Int = 90,
)
