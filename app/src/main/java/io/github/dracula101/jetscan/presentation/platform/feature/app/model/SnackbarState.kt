package io.github.dracula101.jetscan.presentation.platform.feature.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SnackbarState : Parcelable {

    @Parcelize
    data class ShowError(
        val title: String,
        val message: String,
        val errorCode: String? = null,
    ) : SnackbarState()

    @Parcelize
    data class ShowWarning(
        val title: String,
        val message: String,
    ): SnackbarState()

    @Parcelize
    data class ShowSuccess(
        val title: String,
    ): SnackbarState()

}