package io.github.dracula101.jetscan.presentation.features.document.edit

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocument
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val EDIT_DOCUMENT_STATE = "edit_document_state"

@HiltViewModel
class EditDocViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentDao: DocumentDao,
) : BaseViewModel<EditDocState, Unit, EditDocAction>(
    initialState = savedStateHandle[EDIT_DOCUMENT_STATE] ?: EditDocState()
) {

    override fun handleAction(action: EditDocAction) {
        when (action) {
            is EditDocAction.LoadDocument -> {
                handleLoadDocument(action)
            }
        }
    }

    private fun handleLoadDocument(action: EditDocAction.LoadDocument) {
        documentDao
            .getDocumentByUid(action.documentId)
            .onEach { document->
                if (document != null) {
                    mutableStateFlow.update{ it.copy(scannedDocument = document.toDocument()) }
                }
            }
            .launchIn(viewModelScope)
    }

}

@Parcelize
data class EditDocState(
    val scannedDocument: Document? = null
) : Parcelable

sealed class EditDocAction {

    @Parcelize
    data class LoadDocument(val documentId: String) : EditDocAction(), Parcelable

    @Parcelize
    sealed class Ui : EditDocAction(), Parcelable
}