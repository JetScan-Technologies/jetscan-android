package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image

enum class ImageFilter{
    ORIGINAL,
    GRAYSCALE,
    COLOR_BUMP,
    BRIGHTEN,
    COLOR_HALFTONE,
    COLORIZE,
    CONTRAST,
    DIFFUSE,
    SHARPEN;

    fun toFormattedString() : String {
        return when(this){
            ORIGINAL -> "Original"
            GRAYSCALE -> "Grayscale"
            COLOR_BUMP -> "Color Bump"
            BRIGHTEN -> "Brighten"
            COLOR_HALFTONE -> "Color Halftone"
            COLORIZE -> "Colorize"
            CONTRAST -> "Contrast"
            DIFFUSE -> "Diffuse"
            SHARPEN -> "Sharpen"
        }
    }
}