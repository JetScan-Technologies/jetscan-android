package io.github.dracula101.jetscan.presentation.platform.feature.document

enum class MaxDocumentSize {
    SIZE_5MB,
    SIZE_10MB,
    SIZE_20MB,
    SIZE_50MB;

    override fun toString(): String {
        return when(this){
            SIZE_5MB -> "5MB"
            SIZE_10MB -> "10MB"
            SIZE_20MB -> "20MB"
            SIZE_50MB -> "50MB"
        }
    }

    fun toSize(): Int {
        return when(this){
            SIZE_5MB -> 5
            SIZE_10MB -> 10
            SIZE_20MB -> 20
            SIZE_50MB -> 50
        }
    }

    fun toLong(): Int {
        return when(this){
            SIZE_5MB -> 5 * 1024 * 1024
            SIZE_10MB -> 10 * 1024 * 1024
            SIZE_20MB -> 20 * 1024 * 1024
            SIZE_50MB -> 50 * 1024 * 1024
        }
    }

    companion object {
        fun fromSize(size: Int): MaxDocumentSize {
            return when(size){
                5 -> SIZE_5MB
                10 -> SIZE_10MB
                20 -> SIZE_20MB
                50 -> SIZE_50MB
                else -> throw IllegalArgumentException("Invalid size")
            }
        }
    }
}