package io.github.dracula101.pdf.models

enum class PdfQuality {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH;

    override fun toString(): String {
        return when(this) {
            VERY_LOW -> "Very Low"
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
        }
    }

}