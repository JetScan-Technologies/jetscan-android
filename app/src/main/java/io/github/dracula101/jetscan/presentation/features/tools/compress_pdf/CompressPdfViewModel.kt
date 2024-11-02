package io.github.dracula101.jetscan.presentation.features.tools.compress_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepository
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfCompressResult
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfCompressSizesResult
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.presentation.platform.base.ImportBaseViewModel
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.pdf.manager.PdfManager
import io.github.dracula101.pdf.models.PdfCompressionLevel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import javax.inject.Inject

const val COMPRESS_PDF_STATE = ""

@HiltViewModel
class CompressPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentManager: DocumentManager,
    private val documentRepository: DocumentRepository,
    private val configRepository: ConfigRepository,
    private val pdfManager: PdfManager,
    private val pdfToolRepository: PdfToolRepository
) : ImportBaseViewModel<CompressPdfState, Unit, CompressPdfAction>(
    initialState = savedStateHandle[COMPRESS_PDF_STATE] ?: CompressPdfState(),
    documentRepository = documentRepository,
    configRepository = configRepository,
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
            is CompressPdfAction.Ui.GetCompressionSizes -> handleGetCompressionSizes()
            is CompressPdfAction.Ui.CompressPdf -> handleCompressPdf()
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
                compressionLevel = PdfCompressionLevel.MEDIUM
            )
        }
    }

    private fun handleRemoveDocument() {
        mutableStateFlow.update { state ->
            state.copy(
                selectedDocument = null,
                compressionLevel = null,
                compressionSizes = null,
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

    private fun handleGetCompressionSizes() {
        mutableStateFlow.update { state ->
            state.copy(isLoadingCompressionSizes = true)
        }
        viewModelScope.launch {
            val document = stateFlow.value.selectedDocument
            if (document != null) {
                val compressionSizes = pdfToolRepository.getPdfCompressionSizes(
                    document.uri.toFile(),
                    PdfCompressionLevel.entries.map { it.toQuality() }
                )
                when (compressionSizes) {
                    is PdfCompressSizesResult.Success -> {
                        mutableStateFlow.update { state ->
                            state.copy(
                                compressionSizes = compressionSizes.compressSizes
                            )
                        }
                    }
                    is PdfCompressSizesResult.Error -> {
                        // Handle error
                    }
                }
            }
        }.invokeOnCompletion {
            mutableStateFlow.update { state ->
                state.copy(
                    isLoadingCompressionSizes = false
                )
            }
        }
    }

    private fun handleCompressPdf() {
        mutableStateFlow.update { state ->
            state.copy(isLoading = true)
        }
        val tempFile = File.createTempFile("compressed", ".pdf")
        viewModelScope.launch {
            val document = stateFlow.value.selectedDocument
            val compressionLevel = stateFlow.value.compressionLevel
            if (document != null && compressionLevel != null) {
                val compressResult = pdfToolRepository.compressPdfFile(
                    document.uri.toFile(),
                    compressionLevel.toQuality(),
                    outputFile = tempFile
                )
                when (compressResult) {
                    is PdfCompressResult.Success -> {
                        val extraDocumentResponse = documentManager.addExtraDocument(
                            tempFile,
                            "Compressed ${document.name}.pdf",
                        )
                        when (extraDocumentResponse) {
                            is DocManagerResult.Error -> {}
                            is DocManagerResult.Success -> {
                                mutableStateFlow.update { state ->
                                    state.copy(
                                        outputFile = extraDocumentResponse.data,
                                        pdfCompressView = PdfCompressView.COMPLETED
                                    )
                                }

                            }
                        }
                    }
                    is PdfCompressResult.Error -> {
                        // Handle error
                    }
                }
            }
        }.invokeOnCompletion {
            mutableStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                )
            }
            tempFile.delete()
        }
    }

}


@Parcelize
data class CompressPdfState(
    val isLoading: Boolean = false,
    val isLoadingCompressionSizes: Boolean = false,
    val selectedDocument: Document? = null,
    val importDocumentState: ImportDocumentState = ImportDocumentState.Idle,
    val documents: List<Document> = emptyList(),
    val compressionLevel: PdfCompressionLevel? = null,
    val compressionSizes: Map<PdfCompressionLevel, Long>? = null,
    val outputFile: File? = null,
    val pdfCompressView: PdfCompressView = PdfCompressView.DOCUMENT,
) : Parcelable {

    sealed class CompressPdfDialogState : Parcelable {}

}

sealed class CompressPdfAction {

    @Parcelize
    sealed class Ui : CompressPdfAction(), Parcelable {
        data class SelectDocument(val document: Document) : Ui()
        data object RemoveDocument : Ui()
        data class SelectCompressionLevel(val compressionLevel: PdfCompressionLevel) : Ui()
        data object GetCompressionSizes : Ui()
        data object CompressPdf : Ui()
    }

    @Parcelize
    sealed class Internal : CompressPdfAction(), Parcelable {
        data class LoadDocument(val documentId: String) : Internal()
    }

    @Parcelize
    sealed class Alerts : CompressPdfAction(), Parcelable {}
}

enum class PdfCompressView{
    DOCUMENT,
    COMPLETED,
}