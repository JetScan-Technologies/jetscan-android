package io.github.dracula101.pdf.models

enum class PdfQuality {
    LOW,
    MEDIUM,
    HIGH;

    override fun toString(): String {
        return when(this) {
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
        }
    }

}