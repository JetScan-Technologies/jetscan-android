package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera

import androidx.camera.core.AspectRatio

enum class CameraAspectRatio {
    RATIO_4_3,
    RATIO_16_9;

    override fun toString(): String {
        return when (this) {
            RATIO_4_3 -> "4:3"
            RATIO_16_9 -> "16:9"
        }
    }

    fun toFloat(): Float {
        return when (this) {
            RATIO_4_3 -> 3 / 4f
            RATIO_16_9 -> 9 / 16f
        }
    }

    fun toAspectRatio(): Int {
        return when (this) {
            RATIO_4_3 -> AspectRatio.RATIO_4_3
            RATIO_16_9 -> AspectRatio.RATIO_16_9
        }
    }
}