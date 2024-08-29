package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image


enum class ImageOrientation{
    ROTATION_0,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270;

    fun toDegrees(): Float {
        return when(this){
            ROTATION_0 -> 0f
            ROTATION_90 -> 90f
            ROTATION_180 -> 180f
            ROTATION_270 -> 270f
        }
    }

    fun next(): ImageOrientation {
        return when(this){
            ROTATION_0 -> ROTATION_90
            ROTATION_90 -> ROTATION_180
            ROTATION_180 -> ROTATION_270
            ROTATION_270 -> ROTATION_0
        }
    }

    fun previous(): ImageOrientation {
        return when(this){
            ROTATION_0 -> ROTATION_270
            ROTATION_90 -> ROTATION_0
            ROTATION_180 -> ROTATION_90
            ROTATION_270 -> ROTATION_180
        }
    }

    fun isHorizontal(): Boolean {
        return this == ROTATION_0 || this == ROTATION_180
    }
}