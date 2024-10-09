package io.github.dracula101.jetscan.presentation.platform.base

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File

abstract class ImportBaseViewModel<S, E, A>(
    initialState: S,
    private val documentRepository: DocumentRepository,
    private val documentManager: DocumentManager,
    private val pdfManager: PdfManager,
    private val contentResolver: ContentResolver,
) : BaseViewModel<S, E, A>(initialState) {

    private val importDocumentStateFlow: MutableStateFlow<ImportDocumentState> = MutableStateFlow(ImportDocumentState.Idle)
    protected val importDocumentState: StateFlow<ImportDocumentState> = importDocumentStateFlow.asStateFlow()

    fun importDocument(
        uri: Uri,
        imageQuality: ImageQuality,
        password: String? = null,
        passwordRequest: (() -> Unit)? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var tempFile: File? = null
            if (password != null) {
                tempFile = File.createTempFile("temp", ".pdf")
                if (tempFile.exists()) { tempFile.delete() }
                val isDecrypted = pdfManager.decryptPdf(
                    uri = uri,
                    outputFile = tempFile,
                    password = password,
                    contentResolver = contentResolver
                )
                if (!isDecrypted) {
                    importDocumentStateFlow.value = ImportDocumentState.Error(
                        message = "Wrong password",
                        error = Exception("Couldn't decrypt document")
                    )
                    return@launch
                }
            } else {
                val hasPassword = pdfManager.pdfHasPassword(uri, contentResolver)
                if (hasPassword) {
                    if (passwordRequest == null) {
                        importDocumentStateFlow.value = ImportDocumentState.Error(
                            message = "Document is password protected",
                            error = SecurityException("Password protected document")
                        )
                    }
                    else {
                        passwordRequest()
                    }
                    return@launch
                }
            }
            val fileUri = tempFile?.toUri() ?: uri
            importDocumentStateFlow.value = ImportDocumentState.Started(fileUri, imageQuality)
            val fileName = documentManager.getFileName(uri) ?: ""
            val fileLength = documentManager.getFileLength(fileUri)
            documentRepository
                .addImportDocument(
                    uri = fileUri,
                    fileName = fileName,
                    imageQuality = imageQuality,
                    progressListener = { currentProgress, totalProgress ->
                        importDocumentStateFlow.value = ImportDocumentState.InProgress(
                            fileName = fileName,
                            fileLength = fileLength,
                            currentProgress = currentProgress,
                            totalProgress = totalProgress.toFloat()
                        )
                    },
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
