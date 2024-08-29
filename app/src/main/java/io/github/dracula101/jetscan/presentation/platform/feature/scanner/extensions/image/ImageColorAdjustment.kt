package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageColorAdjustment(
    @FloatRange(from = -255.0, to = 255.0)
    val brightness: Float = 0.0f,
    @FloatRange(from = 0.0, to = 10.0)
    val contrast: Float = 1.0f,
    @FloatRange(from = 0.0, to = 5.0)
    val saturation: Float = 1.0f
) : Parcelable {
    companion object {
        val DEFAULT = ImageColorAdjustment()
    }
}