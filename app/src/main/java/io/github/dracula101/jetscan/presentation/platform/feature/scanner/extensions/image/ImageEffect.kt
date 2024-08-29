package io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ImageEffect (
    val orientation: ImageOrientation = ImageOrientation.ROTATION_0,
    val imageFilter: ImageFilter = ImageFilter.ORIGINAL,
    val colorAdjustment: ImageColorAdjustment = ImageColorAdjustment.DEFAULT
) : Parcelable
