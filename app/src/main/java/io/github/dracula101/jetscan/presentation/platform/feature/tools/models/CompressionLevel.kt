package io.github.dracula101.jetscan.presentation.platform.feature.tools.models


enum class CompressionLevel {
    LOW,
    MEDIUM,
    HIGH;

    fun toQuality(): Int {
        return when (this) {
            LOW -> 30
            MEDIUM -> 75
            HIGH -> 90
        }
    }

}
