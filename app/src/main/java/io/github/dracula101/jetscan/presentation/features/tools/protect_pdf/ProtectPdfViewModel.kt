package io.github.dracula101.jetscan.presentation.features.tools.protect_pdf


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val PROTECT_PDF_STATE = ""

@HiltViewModel
class ProtectPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
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
            is ProtectPdfAction.Ui.SelectDocument -> handleSelectDocument(action)
            is ProtectPdfAction.Ui.RemoveDocument -> handleRemoveDocument()
            is ProtectPdfAction.Ui.SetPassword -> handleSetPassword(action)
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
}


@Parcelize
data class ProtectPdfState(
    val selectedDocument: Document? = null,
    val documents: List<Document> = emptyList(),
    val password: String = "",
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
    }

    @Parcelize
    sealed class Alerts : ProtectPdfAction(), Parcelable {}
}