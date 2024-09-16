package io.github.dracula101.jetscan.presentation.features.tools.esign_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val ESIGN_PDF_STATE = "esign_pdf"

@HiltViewModel
class ESignPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<ESignPdfState, Unit, ESignPdfAction>(
    initialState = savedStateHandle[ESIGN_PDF_STATE] ?: ESignPdfState(),
) {
    override fun handleAction(action: ESignPdfAction) {
    }
}


@Parcelize
data class ESignPdfState(
    val isLoading: Boolean = true
) : Parcelable {

    sealed class ESignPdfDialogState : Parcelable {}

}

sealed class ESignPdfAction {

    @Parcelize
    sealed class Ui : ESignPdfAction(), Parcelable {}

    @Parcelize
    sealed class Alerts : ESignPdfAction(), Parcelable {}
}