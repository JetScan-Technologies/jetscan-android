package io.github.dracula101.jetscan.presentation.features.tools.watermark_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val WATERMARK_PDF_STATE = ""

@HiltViewModel
class WatermarkPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<WaterMarkPdfState, Unit, WaterMarkPdfAction>(
    initialState = savedStateHandle[WATERMARK_PDF_STATE] ?: WaterMarkPdfState(),
) {
    override fun handleAction(action: WaterMarkPdfAction) {
    }
}


@Parcelize
data class WaterMarkPdfState(
    val isLoading: Boolean = true
) : Parcelable {

    sealed class WaterMarkPdfDialogState : Parcelable {}

}

sealed class WaterMarkPdfAction {

    @Parcelize
    sealed class Ui : WaterMarkPdfAction(), Parcelable {}

    @Parcelize
    sealed class Alerts : WaterMarkPdfAction(), Parcelable {}
}