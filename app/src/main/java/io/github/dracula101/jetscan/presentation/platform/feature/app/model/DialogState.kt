package io.github.dracula101.jetscan.presentation.platform.feature.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DialogState : Parcelable {

    @Parcelize
    data class ShowError(
        val title: String,
        val message: String,
    ) : DialogState()

    @Parcelize
    data class ShowSuccess(
        val message: String,
    ) : DialogState()

    @Parcelize
    data class ShowConfirmation(
        val title: String,
        val message: String,
        val positiveButtonText: String,
        val negativeButtonText: String,
        val actionInfo: ActionInfo
    ) : DialogState()

    @Parcelize
    data object ShowGeneralDialog : DialogState()
}

enum class ActionInfo {
    DELETE,
    RETRY,
    EDIT,
    CANCEL,
    DONE,
    EXIT
}

