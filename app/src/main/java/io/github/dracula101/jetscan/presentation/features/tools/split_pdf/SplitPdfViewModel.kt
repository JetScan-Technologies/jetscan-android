package io.github.dracula101.jetscan.presentation.features.tools.split_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepository
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfSplitResult
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

const val SPLIT_PDF_STATE = ""

@HiltViewModel
class SplitPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
    private val pdfToolRepository: PdfToolRepository,
    private val documentManager: DocumentManager,
) : BaseViewModel<SplitPdfState, Unit, SplitPdfAction>(
    initialState = savedStateHandle[SPLIT_PDF_STATE] ?: SplitPdfState(),
) {

    init {
        documentRepository
            .getDocuments()
            .onEach { documents ->
                setState {
                    state.copy(documents = documents ?: emptyList())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SplitPdfAction) {
        when (action) {
            is SplitPdfAction.Ui.OnFileNameChanged -> handleFileNameChanged(action.fileName)
            is SplitPdfAction.Ui.OnDocumentSelected -> handleSelectDocument(action.document)
            is SplitPdfAction.Ui.OnDocumentDeleted -> handleDeleteDocument()
            is SplitPdfAction.Ui.AddSplitRange -> handleAddSplitRange(action.range)
            is SplitPdfAction.Ui.OnRangeChanged -> handleRangeChanged(action.range)
            is SplitPdfAction.Ui.OnSplitClicked -> handleSplitClicked()

            is SplitPdfAction.Internal.LoadDocument -> handleLoadDocument(action.documentId)
        }
    }

    private fun handleFileNameChanged(fileName: String) {
        setState {
            state.copy(fileName = fileName)
        }
    }

    private fun handleSelectDocument(document: Document) {
        setState {
            state.copy(selectedDocument = document)
        }
    }

    private fun handleAddSplitRange(range: PdfSplitRange) {
        setState {
            state.copy(
                splitRanges = state.splitRanges + range
            )
        }
    }

    private fun handleRangeChanged(range: String) {
        setState {
            state.copy(rangesString = range)
        }
        val regex = Regex("""^(\d+(-\d+)?)(,(\d+(-\d+)?))*$""")
        if (regex.matches(range)) {
            val ranges = range.split(",").map {
                val split = it.split("-")
                if (split.size == 1) {
                    PdfSplitRange(split[0].toInt())
                } else {
                    PdfSplitRange(split[0].toInt(), split[1].toInt())
                }
            }
            setState {
                state.copy(
                    splitRanges = ranges,
                    hasRangeError = false
                )
            }
        } else {
            setState {
                state.copy(hasRangeError = true)
            }
        }
    }

    private fun handleSplitClicked() {
        val document = state.selectedDocument ?: return
        val ranges = state.splitRanges
        val fileName = state.fileName
        val outputFiles = ranges.map { range->
            File.createTempFile("split-$range", ".pdf")
        }
        setState {
            state.copy(isLoading = true, outputFiles = emptyList())
        }
        viewModelScope.launch {
            val result = pdfToolRepository.splitPdfFile(
                document.uri.toFile(),
                outputFiles,
                state.rangesString
            )
            when (result) {
                is PdfSplitResult.Success -> {
                    result.files.forEachIndexed { index, file ->
                        val outputFileResponse = documentManager
                            .addExtraDocument(
                                file,
                                "${fileName.replace("(Page)", " Page ${ ranges[index] }")}.pdf",
                            )
                        when (outputFileResponse) {
                            is DocManagerResult.Error -> {}
                            is DocManagerResult.Success -> {
                                if (outputFileResponse.data != null){
                                    setState { state.copy(outputFiles = state.outputFiles + outputFileResponse.data) }
                                }
                            }
                        }
                    }
                    setState {
                        state.copy(view = SplitPdfView.COMPLETED)
                    }
                }
                is PdfSplitResult.Error -> {
                }
            }
        }.invokeOnCompletion {
            setState {
                state.copy(isLoading = false)
            }
            outputFiles.forEach { it.delete() }
        }
    }

    private fun handleDeleteDocument() {
        setState {
            state.copy(selectedDocument = null)
        }
    }

    private fun handleLoadDocument(documentId: String) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentByUid(documentId).first()
            setState {
                state.copy(
                    isLoading = false,
                    documentId = documentId,
                    selectedDocument = document,
                )
            }
        }
    }
}


@Parcelize
data class SplitPdfState(
    val isLoading: Boolean = true,
    val documentId: String? = null,
    val documents: List<Document> = emptyList(),
    val selectedDocument: Document? = null,
    val rangesString: String = "",
    val hasRangeError: Boolean = false,
    val outputFiles : List<File> = emptyList(),
    val fileName: String = "JetScan Split (Page) ${
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm a",
            Locale.getDefault()
        ).format(Date())
    }",
    val view: SplitPdfView = SplitPdfView.DOCUMENT,
    val splitRanges: List<PdfSplitRange> = emptyList(),
) : Parcelable {

    sealed class SplitPdfDialogState : Parcelable {}

}

sealed class SplitPdfAction {

    @Parcelize
    sealed class Ui : SplitPdfAction(), Parcelable {
        data class OnFileNameChanged(val fileName: String) : Ui()
        data class OnDocumentSelected(val document: Document) : Ui()
        data class OnRangeChanged(val range: String) : Ui()
        data object OnDocumentDeleted : Ui()
        data class AddSplitRange(val range: PdfSplitRange) : Ui()
        data object OnSplitClicked : Ui()
    }

    @Parcelize
    sealed class Internal : SplitPdfAction(), Parcelable {

        data class LoadDocument(
            val documentId: String
        ) : Internal()

    }

    @Parcelize
    sealed class Alerts : SplitPdfAction(), Parcelable {}
}

enum class SplitPdfView {
    DOCUMENT,
    COMPLETED
}

@Parcelize
data class PdfSplitRange(
    val start: Int,
    val end: Int? = null,
): Parcelable {

    override fun toString(): String {
        return if (end == null) {
            "$start"
        } else {
            "$start-$end"
        }
    }

}