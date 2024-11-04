package io.github.dracula101.jetscan.data.platform.repository.config

import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.MaxDocumentSize
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

interface ConfigRepository {

    /*
    * Onboarding state
    * */
    var isOnboardingCompleted: Boolean
    val isOnboardingCompletedStateFlow: StateFlow<Boolean>
    fun changeOnboardingState(isOnboardingCompleted: Boolean)

    /*
    * First launch state
    * */
    var isFirstLaunch: Boolean
    val isFirstLaunchStateFlow: StateFlow<Boolean>
    fun changeFirstLaunchState(isFirstLaunch: Boolean)

    /*
    * Import Export Quality
    * */
    var importExportQuality: ImageQuality
    val importExportQualityStateFlow: StateFlow<ImageQuality>
    fun changeImportExportQuality(quality: ImageQuality)

    var showImportQualityDialog: Boolean
    val showImportQualityDialogStateFlow: StateFlow<Boolean>
    fun changeShowImportQualityDialog(showImportQualityDialog: Boolean)

    /*
    * Allow Image For Import
    * */
    var allowImageForImport: Boolean
    val allowImageForImportStateFlow: StateFlow<Boolean>
    fun changeAllowImageForImport(allowImageForImport: Boolean)

    /*
    * Document Max Size
    * */
    var maxDocumentSize: MaxDocumentSize
    val maxDocumentSizeStateFlow: StateFlow<MaxDocumentSize>
    fun changeMaxDocumentSize(maxDocumentSize: MaxDocumentSize)

    /*
    * Use App Naming Convention
    * */
    var useAppNamingConvention: Boolean
    val useAppNamingConventionStateFlow: StateFlow<Boolean>
    fun changeUseAppNamingConvention(useAppNamingConvention: Boolean)

    /*
    * Avoid Password Protection Files
    * */
    var avoidPasswordProtectionFiles: Boolean
    val avoidPasswordProtectionFilesStateFlow: StateFlow<Boolean>
    fun changeAvoidPasswordProtectionFiles(avoidPasswordProtectionFiles: Boolean)

    /*
    * PDF Quality
    * */
    var pdfQuality: PdfQuality
    val pdfQualityStateFlow: StateFlow<PdfQuality>
    fun changePdfQuality(quality: PdfQuality)

    /*
    * PDF Page Size
    * */
    var pdfPageSize: PdfPageSize
    val pdfPageSizeStateFlow: StateFlow<PdfPageSize>
    fun changePdfPageSize(pageSize: PdfPageSize)

    var hasPdfMargin: Boolean
    val hasPdfMarginStateFlow: StateFlow<Boolean>
    fun changePdfMargin(hasPdfMargin: Boolean)

    var hasAutoCrop: Boolean
    val hasAutoCropStateFlow: StateFlow<Boolean>
    fun changeAutoCrop(hasAutoCrop: Boolean)

    var documentPrefix: String
    val documentPrefixStateFlow: StateFlow<String>
    fun changeDocumentPrefix(prefix: String)

    var documentSuffix: String?
    val documentSuffixStateFlow: StateFlow<String?>
    fun changeDocumentSuffix(suffix: String?)

    var documentHasDate: Boolean
    val documentHasDateStateFlow: StateFlow<Boolean>
    fun changeDocumentHasDate(hasDate: Boolean)

    var documentHasTime: Boolean
    val documentHasTimeStateFlow: StateFlow<Boolean>
    fun changeDocumentHasTime(hasTime: Boolean)

    var documentDatePattern: DocumentDatePattern
    val documentDatePatternStateFlow: StateFlow<DocumentDatePattern>
    fun changeDocumentDatePattern(datePattern: DocumentDatePattern)

    var documentTimePattern: DocumentTimePattern
    val documentTimePatternStateFlow: StateFlow<DocumentTimePattern>
    fun changeDocumentTimePattern(timePattern: DocumentTimePattern)

    fun getDocumentName(): String
    fun getDocumentName(date: Date): String

    var prioritizeCameraQuality: Boolean
    val prioritizeCameraQualityStateFlow: StateFlow<Boolean>
    fun changePrioritizeCameraQuality(prioritizeCameraQuality: Boolean)

    var cameraGridStatus: Boolean
    val cameraGridStatusStateFlow: StateFlow<Boolean>
    fun changeCameraGridStatus(cameraGridStatus: Boolean)

    var cameraCaptureSoundStatus: Boolean
    val cameraCaptureSoundStatusStateFlow: StateFlow<Boolean>
    fun changeCameraCaptureSoundStatus(cameraCaptureSoundStatus: Boolean)

    var cameraCaptureVibrationStatus: Boolean
    val cameraCaptureVibrationStatusStateFlow: StateFlow<Boolean>
    fun changeCameraCaptureVibrationStatus(cameraCaptureVibrationStatus: Boolean)

}