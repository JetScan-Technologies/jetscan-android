package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class CameraFacingMode : Parcelable {
    BACK,
    FRONT,
}