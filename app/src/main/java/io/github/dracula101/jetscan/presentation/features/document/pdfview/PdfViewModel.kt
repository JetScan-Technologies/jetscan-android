package io.github.dracula101.jetscan.presentation.features.document.pdfview


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocument
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val PDF_VIEW_STATE = "pdf_view_state"

@HiltViewModel
class PdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentDao: DocumentDao,
) : BaseViewModel<PdfState, Unit, PdfViewAction>(
    initialState = savedStateHandle[PDF_VIEW_STATE] ?: PdfState(),
) {
    override fun handleAction(action: PdfViewAction) {
        when (action) {
            is PdfViewAction.Internal.LoadPdfView -> handleLoadPdf(action)
        }
    }

    private fun handleLoadPdf(action: PdfViewAction.Internal.LoadPdfView) {
        viewModelScope.launch(Dispatchers.IO) {
            val document = documentDao
                .getDocumentByUid(action.documentId)
                .first()
            if (document == null) { return@launch }
            mutableStateFlow.update {
                it.copy(
                    isLoading = false,
                    document = document.toDocument()
                )
            }
        }
    }
}


@Parcelize
data class PdfState(
    val isLoading: Boolean = true,
    val document: Document? = null,
) : Parcelable {

    sealed class PdfDialogState : Parcelable {}

}

sealed class PdfViewAction {

    @Parcelize
    sealed class Ui : PdfViewAction(), Parcelable {}

    @Parcelize
    sealed class Internal : PdfViewAction(), Parcelable {
        data class LoadPdfView(val documentId: String) : Internal()
    }

    @Parcelize
    sealed class Alerts : PdfViewAction(), Parcelable {}
}
