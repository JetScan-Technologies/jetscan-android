package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image

enum class ImageFilter{
    ORIGINAL,
    NO_SHADOW,
    AUTO,
    VIBRANT,
    COLOR_BUMP,
    SHARP_BLACK,
    B_W;

    fun toFormattedString() : String {
        return when(this){
            ORIGINAL -> "Original"
            VIBRANT -> "Vibrant"
            NO_SHADOW -> "No Shadow"
            AUTO -> "Auto"
            COLOR_BUMP -> "Color Bump"
            SHARP_BLACK -> "Sharp Black"
            B_W -> "B&W"
        }
    }
}