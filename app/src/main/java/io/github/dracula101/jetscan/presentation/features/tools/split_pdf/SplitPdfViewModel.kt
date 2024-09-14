package io.github.dracula101.jetscan.presentation.features.tools.split_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val SPLIT_PDF_STATE = ""

@HiltViewModel
class SplitPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<SplitPdfState, Unit, SplitPdfAction>(
    initialState = savedStateHandle[SPLIT_PDF_STATE] ?: SplitPdfState(),
) {
    override fun handleAction(action: SplitPdfAction) {
    }
}


@Parcelize
data class SplitPdfState(
    val isLoading: Boolean = true
) : Parcelable {

    sealed class SplitPdfDialogState : Parcelable {}

}

sealed class SplitPdfAction {

    @Parcelize
    sealed class Ui : SplitPdfAction(), Parcelable {}

    @Parcelize
    sealed class Alerts : SplitPdfAction(), Parcelable {}
}