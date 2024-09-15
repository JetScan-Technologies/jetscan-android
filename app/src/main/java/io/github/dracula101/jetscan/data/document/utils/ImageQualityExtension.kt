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
        in 31..50 -> ImageQuality.LOW
        in 51..80 -> ImageQuality.MEDIUM
        else -> ImageQuality.HIGH
    }
}

fun ImageQuality.toBitmapQuality(): Int {
    return when (this) {
        ImageQuality.VERY_LOW -> 50
        ImageQuality.LOW -> 60
        ImageQuality.MEDIUM -> 80
        ImageQuality.HIGH -> 90
    }
}