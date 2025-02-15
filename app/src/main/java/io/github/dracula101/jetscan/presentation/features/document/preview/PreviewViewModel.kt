package io.github.dracula101.jetscan.presentation.features.document.preview

import android.os.Parcelable
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
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
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

const val PREVIEW_STATE = "preview_state"

@HiltViewModel
class PreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentDao: DocumentDao,
) : BaseViewModel<PreviewState, Unit, PreviewAction>(
    initialState = savedStateHandle[PREVIEW_STATE] ?: PreviewState()
) {

    override fun handleAction(action: PreviewAction) {
        when (action) {
            is PreviewAction.LoadDocument -> {
                handleLoadDocument(action)
            }
            is PreviewAction.Ui.Reorder -> {
                handleReorder(action)
            }
        }
    }

    private fun handleLoadDocument(action: PreviewAction.LoadDocument) {
        documentDao
            .getDocumentByUid(action.documentId)
            .onEach { document->
                if (document != null) {
                    mutableStateFlow.update{ it.copy(scannedDocument = document.toDocument()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleReorder(action: PreviewAction.Ui.Reorder) {
        val scannedDocument = stateFlow.value.scannedDocument ?: return
        val newPages = scannedDocument.scannedImages.toMutableList()
        val from = action.from
        val to = action.to
        val page = newPages.removeAt(from)
        newPages.add(to, page)
        val newDocument = scannedDocument.copy(scannedImages = newPages)
        mutableStateFlow.update { it.copy(scannedDocument = newDocument) }
    }
    
}

@Parcelize
data class PreviewState(
    val scannedDocument: Document? = null
) : Parcelable

sealed class PreviewAction {

    @Parcelize
    data class LoadDocument(val documentId: String) : PreviewAction(), Parcelable

    @Parcelize
    sealed class Ui : PreviewAction(), Parcelable {
        data class Reorder(val from: Int, val to: Int) : Ui(), Parcelable
    }
}