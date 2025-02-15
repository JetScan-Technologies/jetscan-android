package io.github.dracula101.jetscan.presentation.features.document.ocr


import android.content.ContentResolver
import android.os.Parcelable
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepository
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfOcrResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.OcrResult
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val OCR_STATE = "ocr_state"

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
    private val pdfToolRepository: PdfToolRepository
) : BaseViewModel<OcrState, Unit, OcrAction>(
    initialState = savedStateHandle[OCR_STATE] ?: OcrState(),
) {
    override fun handleAction(action: OcrAction) {
        when (action) {
            is OcrAction.Internal.LoadDocument -> handleLoadDocument(action.documentId, action.pageIndex)
            is OcrAction.Internal.GetOcrResult -> handleGetOcrResult()
        }
    }

    private fun handleLoadDocument(documentId: String, pageIndex: Int) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentByUid(documentId).first()
            mutableStateFlow.update { it.copy(document = document, pageIndex = pageIndex) }
            if(document!=null){
                trySendAction(OcrAction.Internal.GetOcrResult)
            }
        }
    }

    private fun handleGetOcrResult() {
        val document = stateFlow.value.document ?: return
        if(state.ocrResult != null ) return
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(isLoading = true) }
            val pageIndex = stateFlow.value.pageIndex ?: return@launch
            val imageFile = document.scannedImages[pageIndex].scannedUri.toFile()
            val ocrResult = pdfToolRepository.getOcrPdf(imageFile)
            mutableStateFlow.update {
                it.copy(
                    isLoading = false,
                    ocrResult = when(ocrResult){
                        is PdfOcrResult.Error -> null
                        is PdfOcrResult.Success -> ocrResult.data
                    }
                )
            }
        }
    }

}


@Parcelize
data class OcrState(
    val document: Document? = null,
    val pageIndex: Int? = null,
    val isLoading: Boolean = false,
    val ocrResult: OcrResult? = null,
) : Parcelable {

    sealed class OcrDialogState : Parcelable {}

}

sealed class OcrAction {

    @Parcelize
    sealed class Ui : OcrAction(), Parcelable {}

    @Parcelize
    sealed class Internal : OcrAction(), Parcelable {

        data class LoadDocument(
            val documentId: String,
            val pageIndex: Int,
        ) : Internal()

        data object GetOcrResult : Internal()
    }

    @Parcelize
    sealed class Alerts : OcrAction(), Parcelable {}
}