package io.github.dracula101.jetscan.presentation.features.home.main

import android.content.ContentResolver
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.repository.models.DocumentResult
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState.MainHomeDialogState
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.features.home.main.components.PdfActionPage
import io.github.dracula101.jetscan.presentation.platform.base.ImportBaseViewModel
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

const val MAIN_HOME_STATE = "main_home_state"

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val documentManager: DocumentManager,
    private val pdfManager: PdfManager,
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : ImportBaseViewModel<MainHomeState, Unit, MainHomeAction>(
    initialState = savedStateHandle[MAIN_HOME_STATE] ?: MainHomeState(),
    documentRepository = documentRepository,
    documentManager = documentManager,
    pdfManager = pdfManager,
    contentResolver = contentResolver,
) {

    private val _importJob = MutableStateFlow<Job?>(null)
    private val _fileUri = MutableStateFlow<Uri?>(null)

    init {
        documentRepository
            .getDocuments(excludeFolders = false)
            .onEach { documents ->
                mutableStateFlow.update { state.copy(documents = documents ?: emptyList()) }
            }
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                replay = 1,
            )
            .launchIn(viewModelScope)
        importDocumentState
            .onEach { importState ->
                when(importState){
                    is ImportDocumentState.Idle, is ImportDocumentState.Started -> {}
                    is ImportDocumentState.InProgress -> handleImportDocumentInProgress(importState)
                    is ImportDocumentState.Error -> handleImportDocumentError(importState.message,importState.error)
                    is ImportDocumentState.Success -> handleImportDocumentComplete()
                }
            }
            .launchIn(viewModelScope)
    }


    override fun handleAction(action: MainHomeAction) {
        when (action) {
            is MainHomeAction.Ui.ChangeImportQuality -> handleChangeImportQuality(action.quality)
            is MainHomeAction.Ui.AddDocument -> handleAddDocument(_fileUri.value!!)
            is MainHomeAction.Ui.DeleteDocument -> handleDeleteDocument(action.document)
            is MainHomeAction.Ui.ShowSnackbar -> handleShowSnackbar(action.snackbarState)
            is MainHomeAction.Ui.DismissDialog -> handleDismissDialog()
            is MainHomeAction.Ui.DismissSnackbar -> handleDismissSnackbar()
            is MainHomeAction.Ui.Logout -> handleLogout()
            is MainHomeAction.Ui.ChangeTab -> handleTabChange(action.tab)

            is MainHomeAction.MainHomeNavigate -> handleNavigateTo(action.navigatePage, action.document)
            is MainHomeAction.MainHomeClearNavigate -> handleClearNavigate()


            is MainHomeAction.Alerts.DeleteDocumentAlert -> handleDeleteDocumentAlert(action.document)
            is MainHomeAction.Alerts.ImportDocumentInProgressAlert -> handleImportDocumentInProgressAlert()
            is MainHomeAction.Alerts.ImportQualityAlert -> handleImportQualityAlert(action.uri)
            is MainHomeAction.Alerts.FileNotSelectedAlert -> handleFileNotSelectedAlert()
        }
    }

    private fun handleImportQualityAlert(uri: Uri) {
        _fileUri.value = uri
        mutableStateFlow.update {
            state.copy(
                dialogState = MainHomeDialogState.ShowImportQuality
            )
        }
    }

    private fun handleChangeImportQuality(quality: ImageQuality) {
        mutableStateFlow.update {
            state.copy(
                importQuality = quality
            )
        }
    }

    private fun handleImportDocumentInProgress(
        importState: ImportDocumentState.InProgress
    ) = mutableStateFlow.update { state.copy(importDocumentState = importState) }.also{
        Timber.d("Importing Document: ${importState.fileName} - ${importState.currentProgress} / ${importState.totalProgress}")
    }

    private fun handleNavigateTo(subPage: MainHomeSubPage, document: Document?) {
        mutableStateFlow.update { state.copy(navigateTo = PdfActionPage(page = subPage, document = document)) }
    }

    private fun handleClearNavigate() {
        mutableStateFlow.update { state.copy(navigateTo = null) }
    }

    private fun handleImportDocumentComplete() {
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowSuccess(
                    title = "Document Imported"
                ),
                isImportingDocument = false,
                importDocumentState = null,
            )
        }
    }

    private fun handleImportDocumentError(
        message: String?,
        error: Exception
    ) {
        _importJob.value = null
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowError(
                    title = message ?: "Error Importing Document",
                    message = error.message ?: "Unknown error occurred",
                ),
                isImportingDocument = false,
                importDocumentState = ImportDocumentState.Error(
                    message = message,
                    error = error,
                ),
            )
        }
        viewModelScope.launch {
            delay(2000)
            mutableStateFlow.update {
                state.copy(
                    importDocumentState = null
                )
            }
        }
    }

    private fun handleCancelImportDocument() {
        _importJob.value?.cancel()
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowWarning(
                    title = "Import Document Cancelled",
                    message = "Importing document was cancelled"
                ),
                isImportingDocument = false,
                importDocumentState = null,
            )
        }
    }

    private fun handleDeleteDocumentAlert(scannedDocument: Document) {
        mutableStateFlow.update {
            state.copy(
                dialogState = MainHomeDialogState.ShowDeleteDocument(scannedDocument)
            )
        }
    }

    private fun handleAddDocument(uri: Uri) {
        val fileQuality = state.importQuality
        mutableStateFlow.update { state.copy(isImportingDocument = true) }
        importDocument(
            uri,
            fileQuality,
            passwordRequest = {
                mutableStateFlow.update {
                    state.copy(
                        dialogState = MainHomeDialogState.ShowPasswordDialog(
                            onPasswordEntered = { pass ->
                                importDocument(
                                    uri,
                                    fileQuality,
                                    password = pass,
                                )
                            }
                        )
                    )
                }
            }
        )
    }

    private fun handleDeleteDocument(document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            val deleteResult = documentRepository.deleteDocument(document)
            when(deleteResult){
                is DocumentResult.Success -> {
                    mutableStateFlow.update {
                        state.copy(
                            snackbarState = SnackbarState.ShowSuccess(title = "Document Deleted Successfully"),
                            documents = state.documents.filter { it.id != document.id }
                        )
                    }
                }
                is DocumentResult.Error -> {
                    mutableStateFlow.update {
                        state.copy(
                            snackbarState = SnackbarState.ShowError(
                                title = "Error Deleting Document",
                                message = deleteResult.message,
                                errorCode = deleteResult.type.toString().split(".").last()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun handleShowSnackbar(snackbarState: SnackbarState) {
        mutableStateFlow.update {
            state.copy(
                snackbarState = snackbarState
            )
        }
    }

    private fun handleDismissSnackbar() {
        mutableStateFlow.update { state.copy(snackbarState = null) }
    }

    private fun handleDismissDialog() {
        mutableStateFlow.update { state.copy(dialogState = null) }
    }

    private fun handleImportDocumentInProgressAlert() {
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowWarning(
                    title = "Importing Document",
                    message = "Document is being imported. Please wait."
                )
            )
        }
    }

    private fun handleFileNotSelectedAlert() {
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowWarning(
                    title = "File Not Selected",
                    message = "Please select a file to import."
                )
            )
        }
    }

    private fun handleLogout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private fun handleTabChange(tab: MainHomeTabs) {
        if (tab == state.currentTab) return
        mutableStateFlow.update {
            state.copy(
                currentTab = tab
            )
        }
    }
}

enum class MainHomeTabs {
    HOME,
    FILES,

    //    SUBSCRIPTION,
    SETTINGS;

    fun toLabel(): String {
        return when (this) {
            HOME -> "JetScan"
            FILES -> "Files"
//            SUBSCRIPTION -> "Premium"
            SETTINGS -> "Settings"
        }
    }

    fun toIndex(): Int {
        return when (this) {
            HOME -> 0
            FILES -> 1
//            SUBSCRIPTION -> 2
            SETTINGS -> 3
        }
    }

}

@Parcelize
data class MainHomeState(
    val isImportingDocument: Boolean = false,
    val documents: List<Document> = emptyList(),
    val importDocumentState: ImportDocumentState? = null,
    val importQuality: ImageQuality = ImageQuality.MEDIUM,
    val dialogState: MainHomeDialogState? = null,
    val snackbarState: SnackbarState? = null,
    val navigateTo: PdfActionPage? = null,
    val currentTab: MainHomeTabs = MainHomeTabs.HOME,
) : Parcelable {

    sealed class MainHomeDialogState : Parcelable {

        @Parcelize
        data class ShowDeleteDocument(val document: Document) : MainHomeDialogState(), Parcelable

        @Parcelize
        data object ShowImportQuality : MainHomeDialogState(), Parcelable

        @Parcelize
        data class ShowPasswordDialog(
            val onPasswordEntered: (String) -> Unit
        ) : MainHomeDialogState(), Parcelable
    }

}

sealed class MainHomeAction {

    @Parcelize
    sealed class Ui : MainHomeAction(), Parcelable {
        data object AddDocument : Ui()
        data class ChangeImportQuality(val quality: ImageQuality) : Ui()
        data object Logout : Ui()
        data class DeleteDocument(val document: Document) : Ui()
        data class ShowSnackbar(val snackbarState: SnackbarState) : Ui()
        data object DismissSnackbar : Ui()
        data object DismissDialog : Ui()
        data class ChangeTab(val tab: MainHomeTabs) : Ui()
    }

    @Parcelize
    data class MainHomeNavigate(
        val navigatePage: MainHomeSubPage,
        val document: Document? = null,
    ) : MainHomeAction(), Parcelable

    @Parcelize
    data object MainHomeClearNavigate : MainHomeAction(), Parcelable

    @Parcelize
    sealed class Alerts : MainHomeAction(), Parcelable {
        data object ImportDocumentInProgressAlert : Alerts()
        data class DeleteDocumentAlert(
            val document: Document
        ) : Alerts()

        data object FileNotSelectedAlert : Alerts()
        data class ImportQualityAlert(val uri: Uri) : Alerts()
    }
}