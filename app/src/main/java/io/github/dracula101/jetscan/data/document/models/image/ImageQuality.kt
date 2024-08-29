package io.github.dracula101.jetscan.data.document.models.image

enum class ImageQuality {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH;

    fun toFormattedString() : String {
        return when(this) {
            VERY_LOW -> "Very Low"
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
        }
    }
}