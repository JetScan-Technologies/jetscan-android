package io.github.dracula101.jetscan.presentation.features.document.edit

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.compose.ui.geometry.Size
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.datasource.disk.converters.toDocument
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.repository.models.DocumentResult
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.utils.rotate
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.pdf.manager.PdfManager
import io.github.dracula101.pdf.models.PdfOptions
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val EDIT_DOCUMENT_STATE = "edit_document_state"

@HiltViewModel
class EditDocViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val documentDao: DocumentDao,
    private val openCvManager: OpenCvManager,
    private val documentRepository: DocumentRepository,
    private val pdfManager: PdfManager
) : BaseViewModel<EditDocState, Unit, EditDocAction>(
    initialState = savedStateHandle[EDIT_DOCUMENT_STATE] ?: EditDocState()
) {

    override fun handleAction(action: EditDocAction) {
        when (action) {
            is EditDocAction.LoadDocument -> handleLoadDocument(action)
            is EditDocAction.Ui.ChangeView -> handleViewChange(action)
            is EditDocAction.Ui.ChangeDocumentIndex -> handleDocumentIndexChange(action)
            is EditDocAction.Ui.CropDocument -> handleCropDocument(action)
            is EditDocAction.Ui.RotateDocument -> handleRotateDocument(action)
            is EditDocAction.SaveDocument -> handleSaveDocument()
        }
    }

    private fun handleLoadDocument(action: EditDocAction.LoadDocument) {
        documentDao
            .getDocumentByUid(action.documentId)
            .onEach { document->
                if (document != null) {
                    val currentPage = document.scannedImageEntities.getOrNull(action.documentIndex)
                    mutableStateFlow.update{
                        it.copy(
                            scannedDocument = document.toDocument(),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleViewChange(action: EditDocAction.Ui.ChangeView) {
        mutableStateFlow.update { it.copy(view = action.view) }
    }

    private fun handleDocumentIndexChange(action: EditDocAction.Ui.ChangeDocumentIndex) {
        mutableStateFlow.update { it.copy(documentIndex = action.index) }
    }

    private fun handleCropDocument(action: EditDocAction.Ui.CropDocument){
        mutableStateFlow.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val document = state.scannedDocument ?: return@launch
            val cropCoords = action.cropCoords
            val scannedImageFile = state.scannedDocument?.scannedImages?.getOrNull(state.documentIndex)?.scannedUri?.toFile() ?: return@launch
            val bitmap = BitmapFactory.decodeFile(scannedImageFile.path)
            val croppedBitmap = openCvManager.cropDocument(
                imageBitmap = bitmap,
                imageCropCoords = cropCoords,
            )
            val result = documentRepository
                .updateDocumentImage(
                    bitmap = croppedBitmap,
                    documentUid = document.id,
                    documentImageIndex = state.documentIndex,
                )
        }.invokeOnCompletion {
            mutableStateFlow.update {
                it.copy(isLoading = false, isEdited = true)
            }
        }
    }

    private fun handleRotateDocument(
        action: EditDocAction.Ui.RotateDocument
    ) {
        mutableStateFlow.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val document = state.scannedDocument ?: return@launch
            val scannedImageFile = state.scannedDocument?.scannedImages?.getOrNull(state.documentIndex)?.scannedUri?.toFile() ?: return@launch
            val bitmap = BitmapFactory.decodeFile(scannedImageFile.path)
            val rotatedBitmap = bitmap.rotate(action.rotation.toFloat())
            val result = documentRepository
                .updateDocumentImage(
                    bitmap = rotatedBitmap,
                    documentUid = document.id,
                    documentImageIndex = state.documentIndex,
                )
        }.invokeOnCompletion {
            mutableStateFlow.update {
                it.copy(isLoading = false, isEdited = true)
            }
        }
    }

    private fun handleSaveDocument() {
        val tempFile = File.createTempFile("updated_file", ".pdf")
        if (state.scannedDocument == null ) return
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDocument =
                documentDao.getDocumentByUid(state.scannedDocument!!.id).firstOrNull()?.toDocument()
                    ?: return@launch
            val isSaved = pdfManager.savePdf(
                files = updatedDocument.scannedImages.map { it.scannedUri.toFile() },
                output = tempFile,
                options = PdfOptions(
                    quality = PdfQuality.MEDIUM
                )
            )
            if(isSaved){
                val documentResult = documentRepository.updatePdfDocument(
                    pdf = tempFile,
                    document = state.scannedDocument ?: return@launch
                )
                Timber.i("Document Saved Successfully")
            }
        }
    }
}

@Parcelize
data class EditDocState(
    val scannedDocument: Document? = null,
    val view: EditDocView = EditDocView.PREVIEW,
    val documentIndex: Int = 0,
    val isLoading: Boolean = false,
    val isEdited: Boolean = false,
) : Parcelable

sealed class EditDocAction {

    @Parcelize
    data class LoadDocument(val documentId: String, val documentIndex: Int) : EditDocAction(), Parcelable

    @Parcelize
    sealed class Ui : EditDocAction(), Parcelable {
        data class ChangeView(val view: EditDocView) : Ui()
        data class ChangeDocumentIndex(val index: Int) : Ui()
        data class CropDocument(
            val cropCoords: ImageCropCoords,
        ): Ui()
        data class RotateDocument(
            val rotation: Int,
        ): Ui()
    }

    @Parcelize
    data object SaveDocument : EditDocAction(), Parcelable
}

enum class EditDocView {
    PREVIEW,
    CROP,
    ROTATE,
    FILTER,
}