package io.github.dracula101.jetscan.data.platform.datasource.disk.config

import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.flow.Flow

interface ConfigDiskSource {

    var onboardingComplete: Boolean
    val onboardingCompleteStateFlow: Flow<Boolean>

    var isFirstLaunch: Boolean
    val isFirstLaunchStateFlow: Flow<Boolean>

    var importExportQuality: ImageQuality?
    val importExportQualityStateFlow: Flow<ImageQuality?>

    var showImportQualityDialog: Boolean?
    val showImportQualityDialogStateFlow: Flow<Boolean?>

    var allowImageForImport: Boolean?
    val allowImageForImportStateFlow: Flow<Boolean?>

    var maxDocumentSize: Int?
    val maxDocumentSizeStateFlow: Flow<Int?>

    var useAppNamingConvention: Boolean?
    val useAppNamingConventionStateFlow: Flow<Boolean?>

    var avoidPasswordProtectionFiles: Boolean?
    val avoidPasswordProtectionFilesStateFlow: Flow<Boolean?>

    var pdfQuality: PdfQuality?
    val pdfQualityStateFlow: Flow<PdfQuality?>

    var pdfPageSize: PdfPageSize?
    val pdfPageSizeStateFlow: Flow<PdfPageSize?>

    var hasPdfMargin: Boolean?
    val hasPdfMarginStateFlow: Flow<Boolean?>

    var hasAutoCrop: Boolean?
    val hasAutoCropStateFlow: Flow<Boolean?>

    var documentPrefix: String?
    val documentPrefixStateFlow: Flow<String?>

    var documentSuffix: String?
    val documentSuffixStateFlow: Flow<String?>

    var documentHasDate: Boolean?
    val documentHasDateStateFlow: Flow<Boolean?>

    var documentHasTime: Boolean?
    val documentHasTimeStateFlow: Flow<Boolean?>

    var documentDatePattern: DocumentDatePattern?
    val documentDatePatternStateFlow: Flow<DocumentDatePattern?>

    var documentTimePattern: DocumentTimePattern?
    val documentTimePatternStateFlow: Flow<DocumentTimePattern?>

    var prioritizeCameraQuality: Boolean?
    val prioritizeCameraQualityStateFlow: Flow<Boolean?>

    var cameraGridStatus: Boolean?
    val cameraGridStatusStateFlow: Flow<Boolean?>

    var cameraCaptureSoundStatus: Boolean?
    val cameraCaptureSoundStatusStateFlow: Flow<Boolean?>

    var cameraCaptureVibrationStatus: Boolean?
    val cameraCaptureVibrationStatusStateFlow: Flow<Boolean?>

    fun clearData()

}