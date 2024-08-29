package io.github.dracula101.jetscan.presentation.features.home.main

import android.content.ContentResolver
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.file.FileManager
import io.github.dracula101.jetscan.data.document.manager.file.ScannedDocDirectory
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeState.MainHomeDialogState
import io.github.dracula101.jetscan.presentation.features.home.main.components.MainHomeSubPage
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
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
    private val fileManager: FileManager,
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<MainHomeState, Unit, MainHomeAction>(
    initialState = savedStateHandle[MAIN_HOME_STATE] ?: MainHomeState(),
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
    }


    override fun handleAction(action: MainHomeAction) {
        when (action) {
            is MainHomeAction.Alerts.ImportQualityAlert -> handleImportQualityAlert(action.uri)
            is MainHomeAction.Ui.ChangeImportQuality -> handleChangeImportQuality(action.quality)
            is MainHomeAction.Ui.AddDocument -> handleAddDocument(_fileUri.value!!)
            is MainHomeAction.Ui.DeleteDocument -> handleDeleteDocument(action.document)
            is MainHomeAction.Ui.ShowSnackbar -> handleShowSnackbar(action.snackbarState)
            is MainHomeAction.Ui.DismissDialog -> handleDismissDialog()
            is MainHomeAction.Ui.DismissSnackbar -> handleDismissSnackbar()
            is MainHomeAction.Ui.Logout -> handleLogout()
            is MainHomeAction.Ui.ChangeTab -> handleTabChange(action.tab)

            is MainHomeAction.ImportDocumentState.InProgress -> handleImportDocumentInProgress(action)
            is MainHomeAction.ImportDocumentState.Completed -> handleImportDocumentComplete(action.scannedDocument, action.uri)
            is MainHomeAction.ImportDocumentState.Error -> handleImportDocumentError(action.error)
            is MainHomeAction.ImportDocumentState.Cancelled -> handleCancelImportDocument()

            is MainHomeAction.MainHomeNavigate -> handleNavigateTo(action.navigatePage)
            is MainHomeAction.MainHomeClearNavigate -> handleClearNavigate()


            is MainHomeAction.Alerts.DeleteDocumentAlert -> handleDeleteDocumentAlert(action.document)
            is MainHomeAction.Alerts.ImportDocumentInProgressAlert -> handleImportDocumentInProgressAlert()
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
        action: MainHomeAction.ImportDocumentState
    ) = mutableStateFlow.update { state.copy(importDocumentState = action) }.also {
        if (action is MainHomeAction.ImportDocumentState.InProgress) {
            Timber.i("Importing Document: ${action.currentProgress} / ${action.totalProgress} ")
        }
    }

    private fun handleNavigateTo(subPage: MainHomeSubPage) {
        mutableStateFlow.update { state.copy(navigateTo = subPage) }
    }

    private fun handleClearNavigate() {
        mutableStateFlow.update { state.copy(navigateTo = null) }
    }

    private fun handleImportDocumentComplete(
        scannedDocument: ScannedDocDirectory,
        uri: Uri
    ) {
        val fileSize = documentManager.getFileLength(contentResolver, uri)
        val filename = documentManager.getFileName(contentResolver, uri)
        val scannedImages = scannedDocument.scannedImageDirectory.listFiles()?.map {
            ScannedImage.fromFile(it)
        } ?: emptyList()
        val previewImageUri =
            scannedDocument.previewImage.toUri()
        if (scannedImages.isNotEmpty()) {
            val scannedDoc = Document(
                size = fileSize,
                name = filename ?: "New Document",
                previewImageUri = previewImageUri,
                scannedImages = scannedImages,
                dateCreated = System.currentTimeMillis(),
                uri = uri
            )
            Timber.i("Adding Document")
            viewModelScope.launch {
                documentRepository.addDocument(scannedDoc)
            }.runCatching {
                this
            }.getOrElse { error ->
                Timber.e(error)
                mutableStateFlow.update {
                    state.copy(
                        isImportingDocument = false,
                        importDocumentState = MainHomeAction.ImportDocumentState.Error(
                            message = "Error adding document",
                            error = Exception(error.localizedMessage ?: "Unknown error occurred"),
                        )
                    )
                }
                return@handleImportDocumentComplete
            }
        }
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowSuccess(
                    title = "Document Imported"
                ),
                isImportingDocument = false,
                importDocumentState = null,
            )
        }
        _importJob.value = null
    }

    private fun handleImportDocumentError(error: Exception) {
        _importJob.value = null
        mutableStateFlow.update {
            state.copy(
                snackbarState = SnackbarState.ShowError(
                    title = "Import Document Error",
                    message = error.message ?: "Unknown error occurred",
                ),
                isImportingDocument = false,
                importDocumentState = MainHomeAction.ImportDocumentState.Error(
                    message = "Error importing document",
                    error = error,
                ),
            )
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
        val fileName = documentManager.getFileName(contentResolver, uri) ?: ""
        val fileLength = documentManager.getFileLength(contentResolver, uri)
        val fileQuality = state.importQuality
        mutableStateFlow.update { state.copy(isImportingDocument = true) }
        _importJob.value = viewModelScope.launch(Dispatchers.IO) {
            trySendAction(
                MainHomeAction.ImportDocumentState.InProgress(
                    fileName = fileName,
                    size = fileLength,
                    currentProgress = 0f,
                    totalProgress = 100
                )
            )
            fileManager.addScannedDocument(
                uri,
                imageQuality = fileQuality,
            ) { currentProgress: Float, totalProgress: Int ->
                trySendAction(
                    MainHomeAction.ImportDocumentState.InProgress(
                        fileName = fileName,
                        size = fileLength,
                        currentProgress = currentProgress,
                        totalProgress = totalProgress,
                    )
                )
            }.runCatching {
                this
            }.getOrElse { error ->
                Timber.e(error)
                trySendAction(
                    MainHomeAction.ImportDocumentState.Error(
                        message = "Error importing document",
                        error = Exception(error.message ?: "Unknown error occurred"),
                    )
                )
                return@launch
            }.also { taskFile ->
                when (taskFile) {
                    is Task.Success -> {
                        mutableStateFlow.update {
                            state.copy(
                                importDocumentState = MainHomeAction.ImportDocumentState.InProgress(
                                    fileName = fileName,
                                    size = fileLength,
                                    currentProgress = 100f,
                                    totalProgress = 100
                                )
                            )
                        }
                        // Added delay to show progress bar completion
                        delay(1000)
                        trySendAction(
                            MainHomeAction.ImportDocumentState.Completed(
                                taskFile.data,
                                uri
                            )
                        )
                    }

                    is Task.Error -> {
                        trySendAction(
                            MainHomeAction.ImportDocumentState.Error(
                                message = "Error importing document",
                                error = Exception(
                                    taskFile.error.message ?: "Unknown error occurred"
                                ),
                            )
                        )
                    }

                    is Task.Cancelled -> {
                        trySendAction(
                            MainHomeAction.ImportDocumentState.Error(
                                message = "Importing document was cancelled",
                                error = Exception("Importing document was cancelled")
                            )
                        )
                    }

                    is Task.Idle -> {}
                }
            }
        }
    }

    private fun handleDeleteDocument(document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = documentRepository
                .deleteDocument(document)
                .runCatching {
                    this
                }
                .getOrElse { error ->
                    Timber.e(error)
                    mutableStateFlow.update {
                        state.copy(
                            snackbarState = SnackbarState.ShowError(
                                title = "Error Deleting Document",
                                message = error.message ?: "Unknown error occurred"
                            )
                        )
                    }
                    return@launch
                }
            if (isDeleted) {
                fileManager.deleteScannedDocument(document.name)
            }
            mutableStateFlow.update {
                state.copy(
                    snackbarState =
                    if(isDeleted) SnackbarState.ShowSuccess( title = "Document Deleted" )
                    else SnackbarState.ShowError( title = "Error Deleting Document", message = "Unknown error occurred" ),
                    documents = if (isDeleted) state.documents.filter { it.id != document.id } else state.documents
                )
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
            SETTINGS -> "Account"
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
    val importDocumentState: MainHomeAction.ImportDocumentState? = null,
    val importQuality: ImageQuality = ImageQuality.MEDIUM,
    val dialogState: MainHomeDialogState? = null,
    val snackbarState: SnackbarState? = null,
    val navigateTo: MainHomeSubPage? = null,
    val currentTab: MainHomeTabs = MainHomeTabs.HOME,
) : Parcelable {

    sealed class MainHomeDialogState : Parcelable{

        @Parcelize
        data class ShowDeleteDocument(val document: Document) : MainHomeDialogState(), Parcelable
        @Parcelize
        data object ShowImportQuality : MainHomeDialogState(), Parcelable
    }

}

sealed class MainHomeAction {

    @Parcelize
    sealed class Ui : MainHomeAction(), Parcelable {
        data object AddDocument : Ui()
        data class ChangeImportQuality(val quality: ImageQuality) : Ui()
        data object Logout : Ui()
        data class DeleteDocument(val document: Document) : Ui()
        data class ShowSnackbar(val snackbarState: SnackbarState): Ui()
        data object DismissSnackbar : Ui()
        data object DismissDialog : Ui()
        data class ChangeTab(val tab: MainHomeTabs) : Ui()
    }

    @Parcelize
    data class MainHomeNavigate(
        val navigatePage: MainHomeSubPage
    ): MainHomeAction(), Parcelable

    @Parcelize
    data object MainHomeClearNavigate: MainHomeAction(), Parcelable

    @Parcelize
    sealed class Alerts : MainHomeAction(), Parcelable {
        data object ImportDocumentInProgressAlert : Alerts()
        data class DeleteDocumentAlert(
            val document: Document
        ) : Alerts()
        data object FileNotSelectedAlert : Alerts()
        data class ImportQualityAlert(val uri: Uri) : Alerts()
    }

    @Parcelize
    sealed class ImportDocumentState : MainHomeAction(), Parcelable {
        data class InProgress(
            val fileName: String,
            val size: Long,
            val currentProgress: Float,
            val totalProgress: Int,
        ) : ImportDocumentState()

        data class Completed(
            val scannedDocument: ScannedDocDirectory,
            val uri: Uri,
        ) : ImportDocumentState()

        data class Error(
            val message: String,
            val error: Exception
        ) : ImportDocumentState()

        data object Cancelled : ImportDocumentState()
    }
}