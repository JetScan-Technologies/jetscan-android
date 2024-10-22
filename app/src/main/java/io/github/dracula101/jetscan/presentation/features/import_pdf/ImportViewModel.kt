package io.github.dracula101.jetscan.presentation.features.import_pdf


import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.MainActivity
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.ImportBaseViewModel
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import javax.inject.Inject

const val IMPORT_PDF_STATE = ""

@HiltViewModel
class ImportPdfViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val pdfManager: PdfManager,
    documentRepository: DocumentRepository,
    @ApplicationContext context: Context,
    documentManager: DocumentManager,
    savedStateHandle: SavedStateHandle
) : ImportBaseViewModel<ImportState, Unit, ImportAction>(
    initialState = savedStateHandle[IMPORT_PDF_STATE] ?: ImportState(),
    contentResolver = contentResolver,
    documentRepository = documentRepository,
    pdfManager = pdfManager,
    documentManager = documentManager,
) {

    init {
        importDocumentState
            .onEach { importState ->
                mutableStateFlow.update {
                    it.copy(
                        importDocumentState = importState,
                        isImporting = importState is ImportDocumentState.InProgress
                    )
                }
                if(importState is ImportDocumentState.Success) {
                    // open app
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ImportAction) {
        when (action) {
            ImportAction.Alert.PdfPasswordRequest -> handlePdfPasswordRequest()

            is ImportAction.Ui.ImportDocument -> handleImportDocument()
            is ImportAction.Ui.UnlockPdf -> handleUnlockingPdf(action.password)

            is ImportAction.Internal.LoadPdfUri -> handleLoadPdfUri(action.pdfUri)
            is ImportAction.Internal.SetPdfError -> mutableStateFlow.update { it.copy( hasPdfError = true ) }
        }
    }

    private fun handleLoadPdfUri(pdfUri: Uri) {
        mutableStateFlow.update {
            it.copy(
                pdfUri = pdfUri,
                hasPdfError = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val tempFile = File.createTempFile("import_temp_file", ".pdf")
            contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val tempFileLength = tempFile.length()
            mutableStateFlow.update {
                it.copy(
                    tempFile = tempFile,
                    tempFileBytes = tempFileLength,
                    hasPdfError = tempFileLength == 0L
                )
            }
        }.invokeOnCompletion { throwable ->
            mutableStateFlow.update {
                it.copy( hasPdfError = throwable != null )
            }
        }
    }

    private fun handlePdfPasswordRequest() {
        mutableStateFlow.update {
            it.copy(
                importDialogState = ImportState.ImportDialogState.PasswordRequest
            )
        }
    }

    private fun handleUnlockingPdf(password: String) {
        mutableStateFlow.update { it.copy( importDialogState = null, tempFile = null) }
        val lockedTempFile = state.tempFile
        val tempUnlockedFile = File.createTempFile("import_pdf_unlocked", ".pdf")
        viewModelScope.launch {
            val isSuccess = pdfManager.decryptPdf(
                uri = state.pdfUri!!,
                password = password,
                outputFile = tempUnlockedFile,
                contentResolver = contentResolver
            )
            if (isSuccess) {
                val tempFileLength = tempUnlockedFile.length()
                mutableStateFlow.update {
                    it.copy(
                        tempFile = tempUnlockedFile,
                        tempFileBytes = tempFileLength,
                        hasPdfError = tempFileLength == 0L
                    )
                }
            }else {
                mutableStateFlow.update { it.copy( tempFile = lockedTempFile) }
            }
        }
    }

    private fun handleImportDocument() {
        mutableStateFlow.update { it.copy( isImporting = true ) }
        importDocument(
            uri = state.pdfUri!!,
            imageQuality = ImageQuality.MEDIUM,
            passwordRequest = {
                trySendAction(ImportAction.Alert.PdfPasswordRequest)
            }
        )
    }

    override fun onCleared() {
        state.tempFile?.delete()
        super.onCleared()
    }

}


@Parcelize
data class ImportState(
    val isImporting: Boolean = false,
    val pdfUri: Uri? = null,
    val tempFile: File? = null,
    val tempFileBytes: Long? = null,
    val hasPdfError: Boolean = false,
    val importDialogState: ImportDialogState? = null,
    val importDocumentState: ImportDocumentState? = null
) : Parcelable {

    @Parcelize
    sealed class ImportDialogState : Parcelable {
        data object PasswordRequest: ImportDialogState()
    }

}

sealed class ImportAction {

    @Parcelize
    sealed class Ui : ImportAction(), Parcelable {
        data object ImportDocument: Ui()
        data class UnlockPdf(
            val password: String
        ): Ui()
    }

    @Parcelize
    sealed class Internal : ImportAction(), Parcelable {
        data class LoadPdfUri(val pdfUri: Uri): Internal()
        data object SetPdfError: Internal()
    }


    @Parcelize
    sealed class Alert : ImportAction(), Parcelable {
        data object PdfPasswordRequest : Alert()
    }

}