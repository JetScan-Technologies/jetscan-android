package io.github.dracula101.jetscan.presentation.platform.base

import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

abstract class ImportBaseViewModel<S, E, A>(
    initialState: S,
    private val documentRepository: DocumentRepository,
    private val documentManager: DocumentManager,
) : BaseViewModel<S, E, A>(initialState) {

    private val importDocumentStateFlow: MutableStateFlow<ImportDocumentState> = MutableStateFlow(ImportDocumentState.Idle)
    protected val importDocumentState: StateFlow<ImportDocumentState> = importDocumentStateFlow.asStateFlow()

    fun importDocument(uri: Uri, imageQuality: ImageQuality) {
        importDocumentStateFlow.value = ImportDocumentState.Started(uri, imageQuality)
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = documentManager.getFileName(uri) ?: ""
            val fileLength = documentManager.getFileLength(uri)
            documentRepository
                .addImportDocument(
                    uri = uri,
                    imageQuality = imageQuality,
                    progressListener = { currentProgress, totalProgress ->
                        importDocumentStateFlow.value = ImportDocumentState.InProgress(
                            fileName = fileName,
                            fileLength = fileLength,
                            currentProgress = currentProgress,
                            totalProgress = totalProgress.toFloat()
                        )
                    }
                )
                .runCatching { this }
                .onSuccess {
                    importDocumentStateFlow.value = ImportDocumentState.Success
                }
                .onFailure { error ->
                    importDocumentStateFlow.value = ImportDocumentState.Error(error = Exception(error))
                }
        }
    }

}
@Parcelize
sealed class ImportDocumentState: Parcelable {

    data object Idle: ImportDocumentState()

    data class Started(
        val uri: Uri,
        val imageQuality: ImageQuality,
    ): ImportDocumentState()

    data class InProgress(
        val fileName: String,
        val fileLength: Long,
        val currentProgress: Float,
        val totalProgress: Float,
    ): ImportDocumentState()

    data object Success: ImportDocumentState()

    data class Error(
        val message: String? = null,
        val error: Exception,
    ): ImportDocumentState()
}
