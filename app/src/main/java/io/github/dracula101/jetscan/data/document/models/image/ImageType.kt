package io.github.dracula101.jetscan.data.document.models.image

enum class ImageType {
    PNG,
    JPEG,
    JPG,
    UNKNOWN;

    fun toExtension(): String {
        return when (this) {
            PNG -> "png"
            JPEG -> "jpeg"
            JPG -> "jpg"
            UNKNOWN -> "unknown"
        }
    }
}