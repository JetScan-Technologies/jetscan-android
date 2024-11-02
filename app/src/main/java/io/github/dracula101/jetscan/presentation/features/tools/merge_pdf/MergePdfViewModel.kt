package io.github.dracula101.jetscan.presentation.features.tools.merge_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepository
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfMergeResult
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

const val MERGE_PDF_STATE = ""

@HiltViewModel
class MergePdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
    private val documentManager: DocumentManager,
    private val pdfManager: PdfManager,
    private val configRepository: ConfigRepository,
    private val pdfToolRepository: PdfToolRepository,
) : BaseViewModel<MergePdfState, Unit, MergePdfAction>(
    initialState = savedStateHandle[MERGE_PDF_STATE] ?: MergePdfState(),
) {

    init {
        documentRepository
            .getDocuments(excludeFolders = false)
            .onEach { docs ->
                val documents = docs?.filter { document ->
                    !state.selectedDocuments.contains(document)
                }
                mutableStateFlow.update {
                    it.copy(documents = documents ?: emptyList())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MergePdfAction) {
        when (action) {
            is MergePdfAction.Internal.LoadDocument -> handleLoadDocument(action.documentId)

            is MergePdfAction.Ui.OnFileNameChanged -> handleFileNameChange(action.fileName)
            is MergePdfAction.Ui.OnDocumentSelected -> handleDocumentSelected(action.document)
            is MergePdfAction.Ui.OnDocumentDeleted -> handleDocumentDeleted(action.document)
            is MergePdfAction.Ui.OnMergeDocument -> handleMergeDocument()
        }
    }

    private fun handleLoadDocument(documentId: String) {
        viewModelScope.launch {
            documentRepository
                .getDocumentByUid(documentId)
                .firstOrNull()
                ?.let { document ->
                    mutableStateFlow.update {
                        it.copy(
                            selectedDocuments = it.selectedDocuments + document,
                            documents = it.documents.filter { doc -> doc != document }
                        )
                    }
                }
        }
    }

    private fun handleFileNameChange(fileName: String) {
        mutableStateFlow.update {
            it.copy(fileName = fileName)
        }
    }

    private fun handleDocumentSelected(document: Document) {
        mutableStateFlow.update {
            it.copy(
                selectedDocuments = it.selectedDocuments + document,
                documents = it.documents.filter { doc -> doc != document }
            )
        }
    }

    private fun handleDocumentDeleted(document: Document) {
        mutableStateFlow.update {
            it.copy(
                selectedDocuments = it.selectedDocuments.filter { doc -> doc != document },
                documents = it.documents + document
            )
        }
    }

    private fun handleMergeDocument() {
        val outputFile = File.createTempFile("merged_file", ".pdf")
        mutableStateFlow.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            val mergeResult = pdfToolRepository.mergePdfFiles(
                files = state.selectedDocuments.map { it.uri.toFile() },
                fileName = state.fileName,
                outputFile = outputFile
            )
            if (mergeResult is PdfMergeResult.Error) {
                Timber.e(mergeResult.cause, "Failed to merge PDF files")
                mutableStateFlow.update {
                    it.copy(
                        snackbarState = SnackbarState.ShowError("Merge Failed",mergeResult.message)
                    )
                }
            }
            val docResultFile = documentManager.addExtraDocument(
                file = outputFile,
                fileName = "${state.fileName}.pdf",
            )
            when (docResultFile) {
                is DocManagerResult.Success -> {
                    mutableStateFlow.update {
                        it.copy(
                            mergedDocument = docResultFile.data,
                            view = MergePdfView.MERGED
                        )
                    }
                }
                is DocManagerResult.Error -> {
                    Timber.e(docResultFile.error, "Failed to add merged document")
                    mutableStateFlow.update {
                        it.copy(
                            snackbarState = SnackbarState.ShowError("Merge Failed",docResultFile.message)
                        )
                    }
                }
            }
        }.invokeOnCompletion {
            outputFile.delete()
            mutableStateFlow.update {
                it.copy(isLoading = false)
            }
        }
    }
}


@Parcelize
data class MergePdfState(
    val fileName: String = "JetScan Merged - ${
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm a",
            Locale.getDefault()
        ).format(Date())
    }",
    val documents: List<Document> = emptyList(),
    val selectedDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val mergedDocument: File? = null,
    val view: MergePdfView = MergePdfView.DOCUMENT,
    val dialogState: MergePdfDialogState? = null,
    val snackbarState: SnackbarState? = null,
) : Parcelable {

    sealed class MergePdfDialogState : Parcelable {}

}

sealed class MergePdfAction {

    @Parcelize
    sealed class Ui : MergePdfAction(), Parcelable {
        data class OnFileNameChanged(val fileName: String) : Ui()
        data class OnDocumentSelected(val document: Document) : Ui()
        data class OnDocumentDeleted(val document: Document) : Ui()
        data object OnMergeDocument : Ui()
    }

    @Parcelize
    sealed class Internal : MergePdfAction(), Parcelable {
        data class LoadDocument(val documentId: String) : Internal()
    }

    @Parcelize
    sealed class Alerts : MergePdfAction(), Parcelable {}
}

enum class MergePdfView {
    DOCUMENT,
    MERGED,
}