package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named

const val PROTECT_PDF_STATE = ""

@HiltViewModel
class ProtectPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
    private val pdfManager: PdfManager,
    private val documentManager: DocumentManager,
    @Named("cache") private val cacheDirectory: File,
) : BaseViewModel<ProtectPdfState, Unit, ProtectPdfAction>(
    initialState = savedStateHandle[PROTECT_PDF_STATE] ?: ProtectPdfState(),
) {

    init{
        documentRepository
            .getDocuments(excludeFolders = false)
            .onEach { documents ->
                mutableStateFlow.update { state ->
                    state.copy(documents = documents ?: emptyList())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ProtectPdfAction) {
        when (action) {
            is ProtectPdfAction.Internal.LoadDocument -> handleLoadDocument(action.documentId)

            is ProtectPdfAction.Ui.SelectDocument -> handleSelectDocument(action)
            is ProtectPdfAction.Ui.RemoveDocument -> handleRemoveDocument()
            is ProtectPdfAction.Ui.SetPassword -> handleSetPassword(action)
            is ProtectPdfAction.Ui.ProtectPdf -> handleProtectPdf()

        }
    }

    private fun handleLoadDocument(documentId: String){
        viewModelScope.launch {
            documentRepository
                .getDocumentByUid(documentId)
                .firstOrNull()
                ?.let { document ->
                    mutableStateFlow.update { state ->
                        state.copy(selectedDocument = document)
                    }
                }
        }
    }

    private fun handleSelectDocument(action: ProtectPdfAction.Ui.SelectDocument) {
        mutableStateFlow.update { state ->
            state.copy(selectedDocument = action.document)
        }
    }

    private fun handleRemoveDocument() {
        mutableStateFlow.update { state ->
            state.copy(selectedDocument = null)
        }
    }

    private fun handleSetPassword(action: ProtectPdfAction.Ui.SetPassword) {
        mutableStateFlow.update { state ->
            state.copy(
                password = action.password ?: state.password,
            )
        }
    }

    private fun handleProtectPdf() {
        val state = mutableStateFlow.value
        val selectedDocument = state.selectedDocument ?: return
        val password = state.password

        viewModelScope.launch(Dispatchers.IO) {
            val fileExtension = documentManager.getExtension(selectedDocument.uri).toString()
            val fileName = documentManager.getFileName(selectedDocument.uri) + "_protected$fileExtension"
            val cachedFile = File(cacheDirectory, fileName).apply { createNewFile() }
            val protected = pdfManager.encryptPdf(
                inputFile = selectedDocument.uri.toFile(),
                outputFile = cachedFile,
                password = password,
                masterPassword = password, // master password is same as password (change if needed)
            )
            val savedProtectedFileResult = documentManager.addExtraDocument(
                file = cachedFile,
                fileName = fileName,
            )
            Timber.i("Protected: $savedProtectedFileResult")
            cachedFile.delete()
            when(savedProtectedFileResult){
                is DocManagerResult.Success -> {
                    if(protected) {
                        mutableStateFlow.update {
                            it.copy(
                                fileName = fileName,
                                view = ProtectPdfView.COMPLETED_VIEW,
                                protectedPdf = savedProtectedFileResult.data,
                            )
                        }
                    }
                }
                is DocManagerResult.Error -> {
                    Timber.e("Error: ${savedProtectedFileResult.message}")
                }
            }
        }
    }
}


@Parcelize
data class ProtectPdfState(
    val selectedDocument: Document? = null,
    val documents: List<Document> = emptyList(),
    val fileName: String = "",
    val password: String = "",
    val view: ProtectPdfView = ProtectPdfView.OPERATION_VIEW,
    val protectedPdf: File? = null,
) : Parcelable {

    sealed class ProtectPdfDialogState : Parcelable {}

}

sealed class ProtectPdfAction {

    @Parcelize
    sealed class Ui : ProtectPdfAction(), Parcelable {
        data class SelectDocument(val document: Document) : Ui()
        data object RemoveDocument : Ui()
        data class SetPassword(
            val password: String? = null,
        ) : Ui()
        data object ProtectPdf : Ui()
    }

    @Parcelize
    sealed class Internal : ProtectPdfAction(), Parcelable {
        data class LoadDocument(val documentId: String) : Internal()
    }

    @Parcelize
    sealed class Alerts : ProtectPdfAction(), Parcelable {}
}

enum class ProtectPdfView {
    OPERATION_VIEW,
    COMPLETED_VIEW,
}