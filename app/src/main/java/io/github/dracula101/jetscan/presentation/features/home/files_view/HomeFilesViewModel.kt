package io.github.dracula101.jetscan.presentation.features.home.files_view

import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject


const val HOME_FILES_STATE = "home_files_state"

@HiltViewModel
class HomeFilesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<HomeFilesState, Unit, HomeFilesAction>(
    initialState = savedStateHandle[HOME_FILES_STATE] ?: HomeFilesState(),
) {

    init {
        documentRepository
            .getDocuments()
            .onEach { documents ->
                mutableStateFlow.update { state.copy(documents = documents ?: emptyList()) }
            }
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(1000L),
                replay = 1,
            )
            .launchIn(viewModelScope)
        documentRepository
            .getFolders(DocumentFolder.ROOT_FOLDER)
            .onEach { folders ->
                mutableStateFlow.update { state.copy(folders = folders ?: emptyList()) }
            }
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(1000L),
                replay = 1,
            )
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeFilesAction) {
        when(action){
            is HomeFilesAction.Ui.AddFolder -> handleAddFolder(action.folderName)
            is HomeFilesAction.Ui.FilterDocument -> {}
            is HomeFilesAction.Ui.DismissDialog -> handleDismissAlert(dialog = true)
            is HomeFilesAction.Ui.DismissSnackbar -> handleDismissAlert(snackbar = true)
            is HomeFilesAction.Ui.DeleteFolder -> handleDeleteFolder(action.folder)

            is HomeFilesAction.Alerts.ShowAddFolderAlert -> handleShowAddFolderAlert()
            is HomeFilesAction.Alerts.ShowDeleteFolderAlert -> handleShowDeleteFolderAlert(action.folder)
        }
    }

    private fun handleAddFolder(folderName: String) {
        viewModelScope.launch {
            val isFolderAdded = documentRepository.addFolder(folderName, DocumentFolder.ROOT_FOLDER)
            if (isFolderAdded) {
                mutableStateFlow.update {
                    state.copy(dialogState = null, snackbarState = SnackbarState.ShowSuccess("Folder added successfully"))
                }
            } else {
                mutableStateFlow.update {
                    state.copy(dialogState = null, snackbarState = SnackbarState.ShowError("Failed to add folder", "Error adding to DB"))
                }
            }
        }
    }

    private fun handleShowAddFolderAlert() {
        mutableStateFlow.update {
            state.copy(dialogState = HomeFilesDialogState.ShowAddFolderDialog)
        }
    }

    private fun handleShowDeleteFolderAlert(folder: DocumentFolder) {
        mutableStateFlow.update {
            state.copy(dialogState = HomeFilesDialogState.ShowDeleteFolderDialog(folder))
        }
    }

    private fun handleDismissAlert(dialog: Boolean = false, snackbar: Boolean = false) {
        mutableStateFlow.update {
            state.copy(
                dialogState = if (dialog) null else state.dialogState,
                snackbarState = if (snackbar) null else state.snackbarState,
            )
        }
    }

    private fun handleDeleteFolder(folder: DocumentFolder) {
        viewModelScope.launch {
            val isFolderDeleted = documentRepository.deleteFolder(folder)
            if (isFolderDeleted) {
                mutableStateFlow.update {
                    state.copy(dialogState = null, snackbarState = SnackbarState.ShowSuccess("Folder deleted successfully"))
                }
            } else {
                mutableStateFlow.update {
                    state.copy(dialogState = null, snackbarState = SnackbarState.ShowError("Failed to delete folder", "Error deleting from DB"))
                }
            }
        }

    }
}


@Parcelize
data class HomeFilesState(
    val documents: List<Document> = emptyList(),
    val folders: List<DocumentFolder> = emptyList(),
    val dialogState: HomeFilesDialogState? = null,
    val snackbarState: SnackbarState? = null,
) : Parcelable

@Parcelize
sealed class HomeFilesDialogState : Parcelable {
    data object ShowAddFolderDialog : HomeFilesDialogState()
    data class ShowDeleteFolderDialog(val folder: DocumentFolder) : HomeFilesDialogState()
}

sealed class HomeFilesAction {

    @Parcelize
    sealed class Ui : HomeFilesAction(), Parcelable {
        data class FilterDocument(val filterBy: FilterOption) : Ui()
        data class AddFolder(val folderName: String) : Ui()
        data class DeleteFolder(val folder: DocumentFolder) : Ui()

        data object DismissDialog: Ui()
        data object DismissSnackbar: Ui()
    }


    @Parcelize
    sealed class Alerts : HomeFilesAction(), Parcelable {
        data object ShowAddFolderAlert : Alerts()
        data class ShowDeleteFolderAlert(val folder: DocumentFolder) : Alerts()
    }
}

enum class FilterOption {
    ALL,
    NAME_ASC,
    NAME_DESC,
    DATE,
    SIZE;

    override fun toString(): String {
        return when (this) {
            ALL -> "All"
            NAME_ASC -> "Title (A to Z)"
            NAME_DESC -> "Title (Z to A)"
            DATE -> "Date Created"
            SIZE -> "Size"
        }
    }

}