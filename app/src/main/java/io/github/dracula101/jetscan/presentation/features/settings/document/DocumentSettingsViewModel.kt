package io.github.dracula101.jetscan.presentation.features.settings.document


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.MaxDocumentSize
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val DOCUMENT_SETTINGS_STATE = ""

@HiltViewModel
class DocumentSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val configRepository: ConfigRepository,
) : BaseViewModel<DocumentSettingsState, Unit, DocumentSettingsAction>(
    initialState = savedStateHandle[DOCUMENT_SETTINGS_STATE] ?: DocumentSettingsState(),
) {
    init {
        configRepository.importExportQualityStateFlow.onEach { quality ->
            mutableStateFlow.update { state.copy(importExportQuality = quality) }
        }.launchIn(viewModelScope)

        configRepository.allowImageForImportStateFlow.onEach { allowImageForImport ->
            mutableStateFlow.update { state.copy(allowImageForImport = allowImageForImport) }
        }.launchIn(viewModelScope)

        configRepository.maxDocumentSizeStateFlow.onEach { size ->
            mutableStateFlow.update { state.copy(maxDocumentSize = size) }
        }.launchIn(viewModelScope)

        configRepository.useAppNamingConventionStateFlow.onEach { useAppNamingConvention ->
            mutableStateFlow.update { state.copy(useAppNamingConvention = useAppNamingConvention) }
        }.launchIn(viewModelScope)

        configRepository.avoidPasswordProtectionFilesStateFlow.onEach { avoidPasswordProtectionFiles ->
            mutableStateFlow.update { state.copy(avoidPasswordProtectionFiles = avoidPasswordProtectionFiles) }
        }.launchIn(viewModelScope)

        configRepository.pdfQualityStateFlow.onEach { quality ->
            mutableStateFlow.update { state.copy(pdfQuality = quality) }
        }.launchIn(viewModelScope)

        configRepository.pdfPageSizeStateFlow.onEach { pageSize ->
            mutableStateFlow.update { state.copy(pdfPageSize = pageSize) }
        }.launchIn(viewModelScope)

        configRepository.hasAutoCropStateFlow.onEach { hasAutoCrop ->
            mutableStateFlow.update { state.copy(hasAutoCrop = hasAutoCrop) }
        }.launchIn(viewModelScope)

        configRepository.hasPdfMarginStateFlow.onEach { hasPdfMargin ->
            mutableStateFlow.update { state.copy(hasPdfMargin = hasPdfMargin) }
        }.launchIn(viewModelScope)

        configRepository.documentPrefixStateFlow.onEach { prefix ->
            mutableStateFlow.update { state.copy(documentPrefix = prefix) }
        }.launchIn(viewModelScope)

        configRepository.documentSuffixStateFlow.onEach { suffix ->
            mutableStateFlow.update { state.copy(documentSuffix = suffix) }
        }.launchIn(viewModelScope)

        configRepository.documentHasDateStateFlow.onEach { hasDate ->
            mutableStateFlow.update { state.copy(documentHasDate = hasDate) }
        }.launchIn(viewModelScope)

        configRepository.documentHasTimeStateFlow.onEach { hasTime ->
            mutableStateFlow.update { state.copy(documentHasTime = hasTime) }
        }.launchIn(viewModelScope)

        configRepository.documentDatePatternStateFlow.onEach { datePattern ->
            mutableStateFlow.update { state.copy(documentDatePattern = datePattern) }
        }.launchIn(viewModelScope)

        configRepository.documentTimePatternStateFlow.onEach { timePattern ->
            mutableStateFlow.update { state.copy(documentTimePattern = timePattern) }
        }.launchIn(viewModelScope)

        configRepository.prioritizeCameraQualityStateFlow.onEach { prioritizeCameraQuality ->
            mutableStateFlow.update { state.copy(prioritizeCameraQuality = prioritizeCameraQuality) }
        }.launchIn(viewModelScope)

        configRepository.cameraGridStatusStateFlow.onEach { cameraGridStatus ->
            mutableStateFlow.update { state.copy(cameraGridStatus = cameraGridStatus) }
        }.launchIn(viewModelScope)

        configRepository.cameraCaptureSoundStatusStateFlow.onEach { cameraCaptureSound ->
            mutableStateFlow.update { state.copy(cameraCaptureSound = cameraCaptureSound) }
        }.launchIn(viewModelScope)

        configRepository.cameraCaptureVibrationStatusStateFlow.onEach { cameraCaptureVibration ->
            mutableStateFlow.update { state.copy(cameraCaptureVibration = cameraCaptureVibration) }
        }.launchIn(viewModelScope)

        configRepository.cameraCaptureSoundStatusStateFlow.onEach { cameraCaptureSound ->
            mutableStateFlow.update { state.copy(cameraCaptureSound = cameraCaptureSound) }
        }.launchIn(viewModelScope)
    }

    override fun handleAction(action: DocumentSettingsAction) {
        when(action){
            is DocumentSettingsAction.Ui.ImportExportQuality.ChangeImportExportQuality -> changeImportExportQuality(action.quality)
            is DocumentSettingsAction.Ui.ImportExportQuality.ChangeAllowImageForImport -> changeAllowImageForImport()
            is DocumentSettingsAction.Ui.ImportExportQuality.ChangeMaxDocumentSize -> changeMaxDocumentSize(action.size)
            is DocumentSettingsAction.Ui.ImportExportQuality.ChangeUseAppNamingConvention -> changeUseAppNamingConvention()
            is DocumentSettingsAction.Ui.ImportExportQuality.ChangeAvoidPasswordProtectionFiles -> changeAvoidPasswordProtectionFiles()

            is DocumentSettingsAction.Ui.PdfSettings.ChangeAutoCrop -> changeAutoCrop()
            is DocumentSettingsAction.Ui.PdfSettings.ChangePdfMargin -> changePdfMargin()
            is DocumentSettingsAction.Ui.PdfSettings.ChangePdfPageSize -> changePdfPageSize(action.pageSize)
            is DocumentSettingsAction.Ui.PdfSettings.ChangePdfQuality -> changePdfQuality(action.quality)

            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentHasDate -> changeDocumentHasDate()
            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentHasTime -> changeDocumentHasTime()
            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentPrefix -> changeDocumentPrefix(action.prefix)
            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentSuffix -> changeDocumentSuffix(action.suffix)
            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentDatePattern -> changeDocumentDatePattern(action.datePattern)
            is DocumentSettingsAction.Ui.DocConfig.ChangeDocumentTimePattern -> changeDocumentTimePattern(action.timePattern)

            is DocumentSettingsAction.Ui.CameraConfig.ChangePrioritizeCameraQuality -> changePrioritizeCameraQuality()
            is DocumentSettingsAction.Ui.CameraConfig.ChangeCameraGridStatus -> changeCameraGridStatus()
            is DocumentSettingsAction.Ui.CameraConfig.ChangeCameraCaptureSound -> changeCameraCaptureSound()
            is DocumentSettingsAction.Ui.CameraConfig.ChangeCameraCaptureVibration -> changeCameraCaptureVibration()
        }
    }

    private fun changeImportExportQuality(quality: ImageQuality) = configRepository.changeImportExportQuality(quality)

    private fun changeAllowImageForImport() = configRepository.changeAllowImageForImport(!state.allowImageForImport)

    private fun changePdfQuality(quality: PdfQuality) = configRepository.changePdfQuality(quality)

    private fun changePdfPageSize(pageSize: PdfPageSize) = configRepository.changePdfPageSize(pageSize)

    private fun changeAutoCrop() = configRepository.changeAutoCrop(!state.hasAutoCrop)

    private fun changePdfMargin() = configRepository.changePdfMargin(!state.hasPdfMargin)

    private fun changeDocumentPrefix(prefix: String) = configRepository.changeDocumentPrefix(prefix)

    private fun changeDocumentSuffix(suffix: String) = configRepository.changeDocumentSuffix(suffix)

    private fun changeDocumentHasDate() = configRepository.changeDocumentHasDate(!state.documentHasDate)

    private fun changeDocumentHasTime() = configRepository.changeDocumentHasTime(!state.documentHasTime)

    private fun changeDocumentDatePattern(datePattern: DocumentDatePattern) = configRepository.changeDocumentDatePattern(datePattern)

    private fun changeDocumentTimePattern(timePattern: DocumentTimePattern) = configRepository.changeDocumentTimePattern(timePattern)

    private fun changeMaxDocumentSize(size: MaxDocumentSize) = configRepository.changeMaxDocumentSize(size)

    private fun changeUseAppNamingConvention() = configRepository.changeUseAppNamingConvention(!state.useAppNamingConvention)

    private fun changeAvoidPasswordProtectionFiles() = configRepository.changeAvoidPasswordProtectionFiles(!state.avoidPasswordProtectionFiles)

    private fun changePrioritizeCameraQuality() = configRepository.changePrioritizeCameraQuality(!state.prioritizeCameraQuality)

    private fun changeCameraGridStatus() = configRepository.changeCameraGridStatus(!state.cameraGridStatus)

    private fun changeCameraCaptureSound() = configRepository.changeCameraCaptureSoundStatus(!state.cameraCaptureSound)

    private fun changeCameraCaptureVibration() = configRepository.changeCameraCaptureVibrationStatus(!state.cameraCaptureVibration)

    override fun onCleared() {
        super.onCleared()
        savedStateHandle[DOCUMENT_SETTINGS_STATE] = state
    }
}


@Parcelize
data class DocumentSettingsState(
    val importExportQuality: ImageQuality = ImageQuality.HIGH,
    val allowImageForImport: Boolean = false,
    val maxDocumentSize: MaxDocumentSize = MaxDocumentSize.SIZE_50MB,
    val useAppNamingConvention: Boolean = false,
    val avoidPasswordProtectionFiles: Boolean = false,

    val pdfQuality: PdfQuality = PdfQuality.HIGH,
    val pdfPageSize: PdfPageSize = PdfPageSize.A4,
    val hasPdfMargin: Boolean = false,
    val hasAutoCrop: Boolean = false,

    val documentPrefix: String = "",
    val documentSuffix: String? = null,
    val documentHasDate: Boolean = false,
    val documentHasTime: Boolean = false,
    val documentDatePattern: DocumentDatePattern = DocumentDatePattern.MEDIUM_DATE,
    val documentTimePattern: DocumentTimePattern = DocumentTimePattern.DEFAULT,

    val prioritizeCameraQuality: Boolean = true,
    val cameraGridStatus: Boolean = false,
    val cameraCaptureSound: Boolean = false,
    val cameraCaptureVibration: Boolean = true,
) : Parcelable {

    sealed class DialogState : Parcelable {}

}

sealed class DocumentSettingsAction {

    @Parcelize
    sealed class Ui : DocumentSettingsAction(), Parcelable {

        sealed class ImportExportQuality : Ui() {
            @Parcelize
            class ChangeImportExportQuality(
                val quality: ImageQuality
            ) : ImportExportQuality()

            @Parcelize object ChangeAllowImageForImport : ImportExportQuality()
            @Parcelize class ChangeMaxDocumentSize(
                val size: MaxDocumentSize
            ) : ImportExportQuality()
            @Parcelize object ChangeUseAppNamingConvention : ImportExportQuality()
            @Parcelize object ChangeAvoidPasswordProtectionFiles : ImportExportQuality()
        }

        sealed class PdfSettings : Ui() {
            @Parcelize class ChangePdfQuality(
                val quality: PdfQuality
            ) : PdfSettings()
            @Parcelize class ChangePdfPageSize(
                val pageSize: PdfPageSize
            ) : PdfSettings()
            @Parcelize object ChangePdfMargin: PdfSettings()
            @Parcelize object ChangeAutoCrop : PdfSettings()
        }

        sealed class DocConfig: Ui() {
            @Parcelize class ChangeDocumentPrefix(
                val prefix: String
            ) : DocConfig()
            @Parcelize class ChangeDocumentSuffix(
                val suffix: String
            ) : DocConfig()
            @Parcelize object ChangeDocumentHasDate : DocConfig()
            @Parcelize object ChangeDocumentHasTime : DocConfig()
            @Parcelize class ChangeDocumentDatePattern(
                val datePattern: DocumentDatePattern
            ) : DocConfig()
            @Parcelize class ChangeDocumentTimePattern(
                val timePattern: DocumentTimePattern
            ) : DocConfig()
        }

        sealed class CameraConfig: Ui() {
            @Parcelize object ChangePrioritizeCameraQuality : CameraConfig()
            @Parcelize object ChangeCameraGridStatus : CameraConfig()
            @Parcelize object ChangeCameraCaptureSound : CameraConfig()
            @Parcelize object ChangeCameraCaptureVibration : CameraConfig()
        }

    }

    @Parcelize
    sealed class Alerts : DocumentSettingsAction(), Parcelable {}
}