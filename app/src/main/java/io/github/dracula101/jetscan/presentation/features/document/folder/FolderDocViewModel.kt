package io.github.dracula101.jetscan.presentation.features.document.folder


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

const val FOLDER_DOC_STATE = "folder_state"

@HiltViewModel
class FolderDocViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentRepository: DocumentRepository,
) : BaseViewModel<FolderDocState, Unit, FolderDocAction>(
    initialState = savedStateHandle[FOLDER_DOC_STATE] ?: FolderDocState(),
) {

    init{
        documentRepository
            .getDocuments()
            .onEach { documents->
                mutableStateFlow.update { state.copy(remDocuments = documents ?: emptyList()) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: FolderDocAction) {
        when (action) {
            is FolderDocAction.Ui.ShowDocuments -> handleDocumentState(isShown = true)
            is FolderDocAction.Ui.HideDocuments -> handleDocumentState(isShown = false)
            is FolderDocAction.Ui.AddDocument -> handleAddDocument(action.document)
            is FolderDocAction.Ui.AddFolder -> handleAddFolder(action.folder)
            is FolderDocAction.Ui.RemoveDocument -> handleRemoveDocument(action.document)
            is FolderDocAction.Ui.HandleDocumentSelection -> handleSelectDocument(action.documentUid)

            is FolderDocAction.Internal.LoadFolder -> handleLoadFolder(action)
            is FolderDocAction.Internal.LoadInternalFolders -> handleLoadInternalFolders(action.path, action.name)

            is FolderDocAction.Alerts.ShowAddFolderDialog -> handleAddFolderDialog()
            is FolderDocAction.Alerts.DismissDialog -> handleDismissAlert(dialog = true)
        }
    }

    private fun handleAddDocument(document: Document) {
        if (state.folder == null) {
            return
        }
        viewModelScope.launch {
            documentRepository.addDocumentToFolder(document, state.folder!!)
            trySendAction(FolderDocAction.Ui.HideDocuments)
        }
    }

    private fun handleAddFolder(folderName: String){
        if (state.folder == null) return
        viewModelScope.launch {
            documentRepository.addFolder(
                path = "${state.folder!!.path}/${state.folder!!.name}",
                folderName = folderName,
            )
            mutableStateFlow.update{
                state.copy(
                    dialogState = null,
                )
            }
        }
    }

    private fun handleRemoveDocument(document: Document) {
        if (state.folder == null) {
            return
        }
        viewModelScope.launch {
            documentRepository.deleteDocumentFromFolder(document,state.folder!!)
            trySendAction(FolderDocAction.Ui.HideDocuments)
        }
    }

    private fun handleSelectDocument(documentUid: String) {
        mutableStateFlow.update {
            state.copy(
                documentInfo = state.documentInfo?.map { (document, info) ->
                    if (document.id == documentUid) {
                        document to info.copy(isSelected = !info.isSelected)
                    } else {
                        document to info
                    }
                }
            )
        }
    }

    private fun handleDocumentState(isShown: Boolean) {
        mutableStateFlow.update { state.copy(showDocumentFiles = isShown) }
    }

    private fun handleLoadFolder(action: FolderDocAction.Internal.LoadFolder) {
        documentRepository
            .getFolderByUid(action.folderId)
            .onEach { folder->
                trySendAction(
                    FolderDocAction.Internal.LoadInternalFolders(
                        path = action.path,
                        name = folder?.name ?: ""
                    )
                )
                mutableStateFlow.update {
                    state.copy(
                        folder = folder,
                        isLoading = false,
                        documentInfo = folder?.documents?.map {
                            it to (
                                if (state.documentInfo?.firstOrNull { (doc, _) -> doc.id == it.id } != null) {
                                    state.documentInfo?.first { (doc, _) -> doc.id == it.id }?.second ?: DocumentInfo()
                                }else{
                                    DocumentInfo()
                                }
                            )
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleLoadInternalFolders(path: String, name: String) {
        val newPath = "$path/$name"
        Timber.d("Loading internal folders - $newPath")
        documentRepository
            .getFolders(newPath)
            .onEach { folders->
                mutableStateFlow.update {
                    state.copy(
                        folders = folders,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleAddFolderDialog() {
        mutableStateFlow.update {
            state.copy(
                dialogState = FolderDocState.FolderDocDialogState.ShowAddDocumentDialog
            )
        }
    }

    private fun handleDismissAlert(dialog: Boolean = false, snackbar: Boolean = false){
        mutableStateFlow.update{
            state.copy(
                dialogState = if(dialog) null else state.dialogState
            )
        }
    }
}


@Parcelize
data class FolderDocState(
    val isLoading: Boolean = true,
    val folder: DocumentFolder? = null,
    val showDocumentFiles: Boolean = false,
    val remDocuments: List<Document> = emptyList(),
    val folders: List<DocumentFolder>? = null,
    val documentInfo: List<Pair<Document, DocumentInfo>>? = null,
    val dialogState: FolderDocDialogState? = null,
) : Parcelable {

    @Parcelize
    sealed class FolderDocDialogState : Parcelable {
        data object ShowAddDocumentDialog : FolderDocDialogState()
    }
}

sealed class FolderDocAction {

    @Parcelize
    sealed class Ui : FolderDocAction(), Parcelable {
        data object ShowDocuments : Ui()
        data object HideDocuments : Ui()
        data class AddDocument(val document: Document): Ui()
        data class AddFolder(val folder: String): Ui()
        data class RemoveDocument(val document: Document): Ui()
        data class HandleDocumentSelection(val documentUid: String): Ui()
    }

    @Parcelize
    sealed class Internal :  FolderDocAction(), Parcelable {
        data class LoadFolder(
            val folderId: String,
            val path: String
        ) : Internal()
        data class LoadInternalFolders(
            val path: String,
            val name: String,
        ): Internal()
    }

    @Parcelize
    sealed class Alerts : FolderDocAction(), Parcelable {
        data class ShowAddFolderDialog(val message: String) : Alerts()

        data object DismissDialog: Alerts()
    }
}

data class DocumentInfo(
    val isSelected: Boolean = false
)