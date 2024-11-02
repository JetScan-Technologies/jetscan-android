package io.github.dracula101.pdf.models

enum class PdfCompressionLevel {
    LOW,
    MEDIUM,
    HIGH;


    fun toFormattedString(): String {
        return when(this){
            LOW -> "Low Compression"
            MEDIUM -> "Medium Compression"
            HIGH -> "High Compression"
        }
    }

    fun toSubText(): String {
        return when(this){
            HIGH -> "Smallest size, lower quality"
            MEDIUM -> "Medium size, good quality"
            LOW -> "Largest size, better quality"
        }
    }

    fun toQuality(): Int {
        return when (this) {
            LOW -> 30
            MEDIUM -> 75
            HIGH -> 90
        }
    }
}