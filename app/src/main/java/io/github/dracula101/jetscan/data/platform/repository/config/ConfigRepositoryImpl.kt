package io.github.dracula101.jetscan.data.platform.repository.config

import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.platform.datasource.disk.config.ConfigDiskSource
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.MaxDocumentSize
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date

class ConfigRepositoryImpl(
    private val configDiskSource: ConfigDiskSource,
) : ConfigRepository {

    override var isOnboardingCompleted: Boolean
        get() = configDiskSource.onboardingComplete
        set(value) {
            configDiskSource.onboardingComplete = value
        }

    override val isOnboardingCompletedStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .onboardingCompleteStateFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.onboardingComplete,
            )

    override fun changeOnboardingState(isOnboardingCompleted: Boolean) {
        configDiskSource.onboardingComplete = isOnboardingCompleted
    }

    override var isFirstLaunch: Boolean
        get() = configDiskSource.isFirstLaunch
        set(value) {
            configDiskSource.isFirstLaunch = value
        }

    override val isFirstLaunchStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .isFirstLaunchStateFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.isFirstLaunch,
            )

    override fun changeFirstLaunchState(isFirstLaunch: Boolean) {
        configDiskSource.isFirstLaunch = isFirstLaunch
    }

    override var importExportQuality: ImageQuality
        get() = configDiskSource.importExportQuality ?: DEFAULT_IMPORT_EXPORT_QUALITY
        set(value) {
            configDiskSource.importExportQuality = value
        }

    override val importExportQualityStateFlow: StateFlow<ImageQuality>
        get() = configDiskSource
            .importExportQualityStateFlow
            .map { it ?: DEFAULT_IMPORT_EXPORT_QUALITY }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.importExportQuality ?: DEFAULT_IMPORT_EXPORT_QUALITY,
            )

    override fun changeImportExportQuality(quality: ImageQuality) {
        configDiskSource.importExportQuality = quality
    }

    override var showImportQualityDialog: Boolean
        get() = configDiskSource.showImportQualityDialog ?: true
        set(value) {
            configDiskSource.showImportQualityDialog = value
        }

    override val showImportQualityDialogStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .showImportQualityDialogStateFlow
            .map { it ?: true }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.showImportQualityDialog ?: true,
            )

    override fun changeShowImportQualityDialog(showImportQualityDialog: Boolean) {
        configDiskSource.showImportQualityDialog = showImportQualityDialog
    }


    override var allowImageForImport: Boolean
        get() = configDiskSource.allowImageForImport ?: ALLOW_IMAGE_FOR_IMPORT
        set(value) {
            configDiskSource.allowImageForImport = value
        }

    override val allowImageForImportStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .allowImageForImportStateFlow
            .map { it ?: ALLOW_IMAGE_FOR_IMPORT }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.allowImageForImport ?: ALLOW_IMAGE_FOR_IMPORT,
            )

    override fun changeAllowImageForImport(allowImageForImport: Boolean) {
        configDiskSource.allowImageForImport = allowImageForImport
    }

    override var maxDocumentSize: MaxDocumentSize
        get() = MaxDocumentSize.fromSize(configDiskSource.maxDocumentSize ?: DEFAULT_MAX_DOCUMENT_SIZE.toSize())
        set(value) {
            configDiskSource.maxDocumentSize = value.toSize()
        }

    override val maxDocumentSizeStateFlow: StateFlow<MaxDocumentSize>
        get() = configDiskSource
            .maxDocumentSizeStateFlow
            .map { MaxDocumentSize.fromSize(it ?: DEFAULT_MAX_DOCUMENT_SIZE.toSize()) }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = MaxDocumentSize.fromSize(configDiskSource.maxDocumentSize ?: DEFAULT_MAX_DOCUMENT_SIZE.toSize()),
            )

    override fun changeMaxDocumentSize(maxDocumentSize: MaxDocumentSize) {
        configDiskSource.maxDocumentSize = maxDocumentSize.toSize()
    }

    override var useAppNamingConvention: Boolean
        get() = configDiskSource.useAppNamingConvention ?: DEFAULT_USE_APP_NAMING_CONVENTION
        set(value) {
            configDiskSource.useAppNamingConvention = value
        }

    override val useAppNamingConventionStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .useAppNamingConventionStateFlow
            .map { it ?: DEFAULT_USE_APP_NAMING_CONVENTION }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.useAppNamingConvention ?: DEFAULT_USE_APP_NAMING_CONVENTION,
            )

    override fun changeUseAppNamingConvention(useAppNamingConvention: Boolean) {
        configDiskSource.useAppNamingConvention = useAppNamingConvention
    }

    override var avoidPasswordProtectionFiles: Boolean
        get() = configDiskSource.avoidPasswordProtectionFiles ?: DEFAULT_AVOID_PASSWORD_PROTECTION_FILES
        set(value) {
            configDiskSource.avoidPasswordProtectionFiles = value
        }

    override val avoidPasswordProtectionFilesStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .avoidPasswordProtectionFilesStateFlow
            .map { it ?: DEFAULT_AVOID_PASSWORD_PROTECTION_FILES }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.avoidPasswordProtectionFiles ?: DEFAULT_AVOID_PASSWORD_PROTECTION_FILES,
            )

    override fun changeAvoidPasswordProtectionFiles(avoidPasswordProtectionFiles: Boolean) {
        configDiskSource.avoidPasswordProtectionFiles = avoidPasswordProtectionFiles
    }

    override var pdfQuality: PdfQuality
        get() = configDiskSource.pdfQuality ?: DEFAULT_PDF_QUALITY
        set(value) {
            configDiskSource.pdfQuality = value
        }

    override val pdfQualityStateFlow: StateFlow<PdfQuality>
        get() = configDiskSource
            .pdfQualityStateFlow
            .map { it ?: DEFAULT_PDF_QUALITY }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.pdfQuality ?: DEFAULT_PDF_QUALITY,
            )

    override fun changePdfQuality(quality: PdfQuality) {
        configDiskSource.pdfQuality = quality
    }

    override var pdfPageSize: PdfPageSize
        get() = configDiskSource.pdfPageSize ?: DEFAULT_PDF_PAGE_SIZE
        set(value) {
            configDiskSource.pdfPageSize = value
        }

    override val pdfPageSizeStateFlow: StateFlow<PdfPageSize>
        get() = configDiskSource
            .pdfPageSizeStateFlow
            .map { it ?: DEFAULT_PDF_PAGE_SIZE }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.pdfPageSize ?: DEFAULT_PDF_PAGE_SIZE,
            )

    override fun changePdfPageSize(pageSize: PdfPageSize) {
        configDiskSource.pdfPageSize = pageSize
    }

    override var hasAutoCrop: Boolean
        get() = configDiskSource.hasAutoCrop ?: DEFAULT_HAS_AUTO_CROP
        set(value) {
            configDiskSource.hasAutoCrop = value
        }

    override val hasAutoCropStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .hasAutoCropStateFlow
            .map { it ?: DEFAULT_HAS_AUTO_CROP }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.hasAutoCrop ?: DEFAULT_HAS_AUTO_CROP,
            )

    override fun changeAutoCrop(hasAutoCrop: Boolean) {
        configDiskSource.hasAutoCrop = hasAutoCrop
    }

    override var hasPdfMargin: Boolean
        get() = configDiskSource.hasPdfMargin ?: DEFAULT_HAS_PDF_MARGIN
        set(value) {
            configDiskSource.hasPdfMargin = value
        }

    override val hasPdfMarginStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .hasPdfMarginStateFlow
            .map { it ?: DEFAULT_HAS_PDF_MARGIN }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.hasPdfMargin ?: DEFAULT_HAS_PDF_MARGIN,
            )

    override fun changePdfMargin(hasPdfMargin: Boolean) {
        configDiskSource.hasPdfMargin = hasPdfMargin
    }



    override var documentPrefix: String
        get() = configDiskSource.documentPrefix ?: DEFAULT_DOCUMENT_PREFIX
        set(value) {
            configDiskSource.documentPrefix = value
        }

    override val documentPrefixStateFlow: StateFlow<String>
        get() = configDiskSource
            .documentPrefixStateFlow
            .map { it ?: DEFAULT_DOCUMENT_PREFIX }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentPrefix ?: DEFAULT_DOCUMENT_PREFIX,
            )


    override fun changeDocumentPrefix(prefix: String) {
        configDiskSource.documentPrefix = prefix
    }

    override var documentSuffix: String?
        get() = configDiskSource.documentSuffix
        set(value) {
            configDiskSource.documentSuffix = value
        }

    override val documentSuffixStateFlow: StateFlow<String?>
        get() = configDiskSource
            .documentSuffixStateFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentSuffix,
            )

    override fun changeDocumentSuffix(suffix: String?) {
        configDiskSource.documentSuffix = suffix
    }


    override var documentHasDate: Boolean
        get() = configDiskSource.documentHasDate ?: DEFAULT_DOCUMENT_HAS_DATE
        set(value) {
            configDiskSource.documentHasDate = value
        }

    override val documentHasDateStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .documentHasDateStateFlow
            .map { it ?: DEFAULT_DOCUMENT_HAS_DATE }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentHasDate ?: DEFAULT_DOCUMENT_HAS_DATE,
            )

    override fun changeDocumentHasDate(hasDate: Boolean) {
        configDiskSource.documentHasDate = hasDate
    }


    override var documentHasTime: Boolean
        get() = configDiskSource.documentHasTime ?: DEFAULT_DOCUMENT_HAS_TIME
        set(value) {
            configDiskSource.documentHasTime = value
        }

    override val documentHasTimeStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .documentHasTimeStateFlow
            .map { it ?: DEFAULT_DOCUMENT_HAS_TIME }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentHasTime ?: DEFAULT_DOCUMENT_HAS_TIME,
            )

    override fun changeDocumentHasTime(hasTime: Boolean) {
        configDiskSource.documentHasTime = hasTime
    }

    override var documentDatePattern: DocumentDatePattern
        get() = configDiskSource.documentDatePattern ?: DEFAULT_DOCUMENT_DATE_PATTERN
        set(value) {
            configDiskSource.documentDatePattern = value
        }
    override val documentDatePatternStateFlow: StateFlow<DocumentDatePattern>
        get() = configDiskSource
            .documentDatePatternStateFlow
            .map { it ?: DEFAULT_DOCUMENT_DATE_PATTERN }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentDatePattern ?: DEFAULT_DOCUMENT_DATE_PATTERN,
            )

    override fun changeDocumentDatePattern(datePattern: DocumentDatePattern) {
        configDiskSource.documentDatePattern = datePattern
    }

    override var documentTimePattern: DocumentTimePattern
        get() = configDiskSource.documentTimePattern ?: DEFAULT_DOCUMENT_TIME_PATTERN
        set(value) {
            configDiskSource.documentTimePattern = value
        }

    override val documentTimePatternStateFlow: StateFlow<DocumentTimePattern>
        get() = configDiskSource
            .documentTimePatternStateFlow
            .map { it ?: DEFAULT_DOCUMENT_TIME_PATTERN }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.documentTimePattern ?: DEFAULT_DOCUMENT_TIME_PATTERN,
            )

    override fun changeDocumentTimePattern(timePattern: DocumentTimePattern) {
        configDiskSource.documentTimePattern = timePattern
    }

    override fun getDocumentName(): String {
        val prefix = configDiskSource.documentPrefix ?: DEFAULT_DOCUMENT_PREFIX
        val suffix = configDiskSource.documentSuffix ?: ""
        val hasDate = configDiskSource.documentHasDate ?: DEFAULT_DOCUMENT_HAS_DATE
        val hasTime = configDiskSource.documentHasTime ?: DEFAULT_DOCUMENT_HAS_TIME
        val datePattern = configDiskSource.documentDatePattern ?: DEFAULT_DOCUMENT_DATE_PATTERN
        val timePattern = configDiskSource.documentTimePattern ?: DEFAULT_DOCUMENT_TIME_PATTERN

        val date = Calendar.getInstance().time
        val dateStr = datePattern.format(date)
        val timeStr = timePattern.format(date)
        return "$prefix ${if (hasDate) dateStr else ""} ${if (hasTime) timeStr else ""}$suffix"
    }

    override fun getDocumentName(date: Date): String {
        val prefix = configDiskSource.documentPrefix ?: DEFAULT_DOCUMENT_PREFIX
        val suffix = configDiskSource.documentSuffix ?: ""
        val hasDate = configDiskSource.documentHasDate ?: DEFAULT_DOCUMENT_HAS_DATE
        val hasTime = configDiskSource.documentHasTime ?: DEFAULT_DOCUMENT_HAS_TIME
        val datePattern = configDiskSource.documentDatePattern ?: DEFAULT_DOCUMENT_DATE_PATTERN
        val timePattern = configDiskSource.documentTimePattern ?: DEFAULT_DOCUMENT_TIME_PATTERN

        val dateStr = datePattern.format(date)
        val timeStr = timePattern.format(date)
        return "$prefix ${if (hasDate) dateStr else ""} ${if (hasTime) timeStr else ""}${if (suffix.isNotEmpty()) " $suffix" else ""}"
    }

    override var prioritizeCameraQuality: Boolean
        get() = configDiskSource.prioritizeCameraQuality ?: DEFAULT_CAMERA_PRIORITIZE_QUALITY
        set(value) {
            configDiskSource.prioritizeCameraQuality = value
        }

    override val prioritizeCameraQualityStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .prioritizeCameraQualityStateFlow
            .map { it ?: DEFAULT_CAMERA_PRIORITIZE_QUALITY }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.prioritizeCameraQuality ?: DEFAULT_CAMERA_PRIORITIZE_QUALITY,
            )

    override fun changePrioritizeCameraQuality(prioritizeCameraQuality: Boolean) {
        configDiskSource.prioritizeCameraQuality = prioritizeCameraQuality
    }

    override var cameraGridStatus: Boolean
        get() = configDiskSource.cameraGridStatus ?: DEFAULT_CAMERA_GRID_STATUS
        set(value) {
            configDiskSource.cameraGridStatus = value
        }

    override val cameraGridStatusStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .cameraGridStatusStateFlow
            .map { it ?: DEFAULT_CAMERA_GRID_STATUS }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.cameraGridStatus ?: DEFAULT_CAMERA_GRID_STATUS,
            )

    override fun changeCameraGridStatus(cameraGridStatus: Boolean) {
        configDiskSource.cameraGridStatus = cameraGridStatus
    }

    override var cameraCaptureSoundStatus: Boolean
        get() = configDiskSource.cameraCaptureSoundStatus ?: DEFAULT_CAMERA_CAPTURE_SOUND_STATUS
        set(value) {
            configDiskSource.cameraCaptureSoundStatus = value
        }

    override val cameraCaptureSoundStatusStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .cameraCaptureSoundStatusStateFlow
            .map { it ?: DEFAULT_CAMERA_CAPTURE_SOUND_STATUS }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.cameraCaptureSoundStatus ?: DEFAULT_CAMERA_CAPTURE_SOUND_STATUS,
            )

    override fun changeCameraCaptureSoundStatus(cameraCaptureSoundStatus: Boolean) {
        configDiskSource.cameraCaptureSoundStatus = cameraCaptureSoundStatus
    }

    override var cameraCaptureVibrationStatus: Boolean
        get() = configDiskSource.cameraCaptureVibrationStatus ?: DEFAULT_CAMERA_CAPTURE_VIBRATION_STATUS
        set(value) {
            configDiskSource.cameraCaptureVibrationStatus = value
        }

    override val cameraCaptureVibrationStatusStateFlow: StateFlow<Boolean>
        get() = configDiskSource
            .cameraCaptureVibrationStatusStateFlow
            .map { it ?: DEFAULT_CAMERA_CAPTURE_VIBRATION_STATUS }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = configDiskSource.cameraCaptureVibrationStatus ?: DEFAULT_CAMERA_CAPTURE_VIBRATION_STATUS,
            )

    override fun changeCameraCaptureVibrationStatus(cameraCaptureVibrationStatus: Boolean) {
        configDiskSource.cameraCaptureVibrationStatus = cameraCaptureVibrationStatus
    }

    companion object {
        // Default Values
        val DEFAULT_IMPORT_EXPORT_QUALITY = ImageQuality.HIGH
        const val ALLOW_IMAGE_FOR_IMPORT = true
        val DEFAULT_MAX_DOCUMENT_SIZE = MaxDocumentSize.SIZE_50MB
        const val DEFAULT_USE_APP_NAMING_CONVENTION = false
        const val DEFAULT_AVOID_PASSWORD_PROTECTION_FILES = false

        val DEFAULT_PDF_QUALITY = PdfQuality.HIGH
        val DEFAULT_PDF_PAGE_SIZE = PdfPageSize.A4
        const val DEFAULT_HAS_PDF_MARGIN = false
        const val DEFAULT_HAS_AUTO_CROP = false

        const val DEFAULT_DOCUMENT_PREFIX = "JetScan"
        const val DEFAULT_DOCUMENT_HAS_DATE = true
        const val DEFAULT_DOCUMENT_HAS_TIME = true
        val DEFAULT_DOCUMENT_DATE_PATTERN = DocumentDatePattern.MEDIUM_DATE
        val DEFAULT_DOCUMENT_TIME_PATTERN = DocumentTimePattern.DEFAULT

        const val DEFAULT_CAMERA_PRIORITIZE_QUALITY = true
        const val DEFAULT_CAMERA_GRID_STATUS = false
        const val DEFAULT_CAMERA_CAPTURE_SOUND_STATUS = true
        const val DEFAULT_CAMERA_CAPTURE_VIBRATION_STATUS = false
    }
}