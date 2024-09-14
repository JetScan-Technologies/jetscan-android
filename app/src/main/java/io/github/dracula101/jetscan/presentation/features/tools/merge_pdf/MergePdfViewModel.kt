package io.github.dracula101.jetscan.presentation.features.tools.merge_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
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
) : BaseViewModel<MergePdfState, Unit, MergePdfAction>(
    initialState = savedStateHandle[MERGE_PDF_STATE] ?: MergePdfState(),
) {

    init {
        documentRepository
            .getDocuments(excludeFolders = false)
            .onEach {
                val documents = it?.filter { document ->
                    !state.selectedDocuments.contains(document)
                }
                mutableStateFlow.update {
                    it.copy(documents = documents ?: emptyList())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MergePdfAction) {
        when(action){
            is MergePdfAction.Ui.OnFileNameChanged -> handleFileNameChange(action.fileName)
            is MergePdfAction.Ui.OnDocumentSelected -> handleDocumentSelected(action.document)
            is MergePdfAction.Ui.OnDocumentDeleted -> handleDocumentDeleted(action.document)
        }
    }

    private fun handleFileNameChange(fileName: String){
        mutableStateFlow.update {
            it.copy(fileName = fileName)
        }
    }

    private fun handleDocumentSelected(document: Document){
        mutableStateFlow.update {
            it.copy(
                selectedDocuments = it.selectedDocuments + document,
                documents = it.documents.filter { doc -> doc != document }
            )
        }
    }

    private fun handleDocumentDeleted(document: Document){
        mutableStateFlow.update {
            it.copy(
                selectedDocuments = it.selectedDocuments.filter { doc -> doc != document },
                documents = it.documents + document
            )
        }
    }
}


@Parcelize
data class MergePdfState(
    val fileName: String = "JetScan Merged - ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}",
    val documents : List<Document> = emptyList(),
    val selectedDocuments : List<Document> = emptyList(),
    val dialogState : MergePdfDialogState? = null,
    val snackbarState : SnackbarState? = null,
) : Parcelable {

    sealed class MergePdfDialogState : Parcelable {}

}

sealed class MergePdfAction {

    @Parcelize
    sealed class Ui : MergePdfAction(), Parcelable {
        data class OnFileNameChanged(val fileName: String) : Ui()
        data class OnDocumentSelected(val document: Document) : Ui()
        data class OnDocumentDeleted(val document: Document) : Ui()
    }

    @Parcelize
    sealed class Alerts : MergePdfAction(), Parcelable {}
}