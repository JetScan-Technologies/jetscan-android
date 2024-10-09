package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.ImportBaseViewModel
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val COMPRESS_PDF_STATE = ""

@HiltViewModel
class CompressPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentManager: DocumentManager,
    private val documentRepository: DocumentRepository,
    private val pdfManager: PdfManager,
) : ImportBaseViewModel<CompressPdfState, Unit, CompressPdfAction>(
    initialState = savedStateHandle[COMPRESS_PDF_STATE] ?: CompressPdfState(),
    documentRepository = documentRepository,
    documentManager = documentManager,
    pdfManager = pdfManager,
    contentResolver = contentResolver
) {

    init {
        documentRepository
            .getDocuments(excludeFolders = false)
            .onEach {
                mutableStateFlow.update { state ->
                    state.copy(documents = it ?: emptyList())
                }
            }
            .launchIn(viewModelScope)
        importDocumentState
            .onEach { importState->
                mutableStateFlow.update { state ->
                    state.copy(importDocumentState = importState)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: CompressPdfAction) {
        when (action) {
            is CompressPdfAction.Ui.SelectDocument -> handleSelectDocument(action)
            is CompressPdfAction.Internal.LoadDocument -> handleLoadDocument(action)
            is CompressPdfAction.Ui.RemoveDocument -> handleRemoveDocument()
            is CompressPdfAction.Ui.SelectCompressionLevel -> handleSelectCompressionLevel(action)
        }
    }

    private fun handleLoadDocument(action: CompressPdfAction.Internal.LoadDocument) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentByUid(action.documentId).firstOrNull()
            document?.let {
                mutableStateFlow.update { state ->
                    state.copy(
                        selectedDocument = document
                    )
                }
            }
        }
    }

    private fun handleSelectDocument(action: CompressPdfAction.Ui.SelectDocument) {
        mutableStateFlow.update { state ->
            state.copy(
                selectedDocument = action.document,
                compressionLevel = CompressionLevel.MEDIUM
            )
        }
    }

    private fun handleRemoveDocument() {
        mutableStateFlow.update { state ->
            state.copy(
                selectedDocument = null,
                compressionLevel = null
            )
        }
    }

    private fun handleSelectCompressionLevel(action: CompressPdfAction.Ui.SelectCompressionLevel) {
        mutableStateFlow.update { state ->
            state.copy(
                compressionLevel = action.compressionLevel
            )
        }
    }
}


@Parcelize
data class CompressPdfState(
    val selectedDocument: Document? = null,
    val importDocumentState: ImportDocumentState = ImportDocumentState.Idle,
    val documents: List<Document> = emptyList(),
    val compressionLevel: CompressionLevel? = null
) : Parcelable {

    sealed class CompressPdfDialogState : Parcelable {}

}

sealed class CompressPdfAction {

    @Parcelize
    sealed class Ui : CompressPdfAction(), Parcelable {
        data class SelectDocument(val document: Document) : Ui()
        data object RemoveDocument : Ui()
        data class SelectCompressionLevel(val compressionLevel: CompressionLevel) : Ui()
    }

    @Parcelize
    sealed class Internal : CompressPdfAction(), Parcelable {
        data class LoadDocument(val documentId: String) : Internal()
    }

    @Parcelize
    sealed class Alerts : CompressPdfAction(), Parcelable {}
}

enum class CompressionLevel{
    LOW,
    MEDIUM,
    HIGH;

    fun toFormattedString(): String {
        return when(this){
            LOW -> "Low Compression"
            MEDIUM -> "Medium Compression"
            HIGH -> "High Compression"
        }
    }

    fun toSubText(): String {
        return when(this){
            LOW -> "Smallest size, lower quality"
            MEDIUM -> "Medium size, good quality"
            HIGH -> "Largest size, better quality"
        }
    }
}