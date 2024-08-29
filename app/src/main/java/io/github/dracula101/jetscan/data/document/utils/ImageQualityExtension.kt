package io.github.dracula101.jetscan.data.document.utils

import io.github.dracula101.jetscan.data.document.models.image.ImageQuality

fun ImageQuality.getImageHeight(): Int {
    return when (this) {
        ImageQuality.VERY_LOW -> 1000
        ImageQuality.LOW -> 3000
        ImageQuality.MEDIUM -> 4000
        ImageQuality.HIGH -> 5000
    }
}

fun Int.fromImageQuality(): ImageQuality {
    return when (this) {
        in 0..30 -> ImageQuality.VERY_LOW
        65 -> ImageQuality.LOW
        85 -> ImageQuality.MEDIUM
        100 -> ImageQuality.HIGH
        else -> ImageQuality.MEDIUM
    }
}

fun ImageQuality.toBitmapQuality(): Int {
    return when (this) {
        ImageQuality.VERY_LOW -> 30
        ImageQuality.LOW -> 65
        ImageQuality.MEDIUM -> 85
        ImageQuality.HIGH -> 100
    }
}