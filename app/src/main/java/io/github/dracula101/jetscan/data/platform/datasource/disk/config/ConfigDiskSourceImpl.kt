package io.github.dracula101.jetscan.data.platform.datasource.disk.config

import android.content.SharedPreferences
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.platform.datasource.disk.BaseDiskSource
import io.github.dracula101.jetscan.data.platform.repository.util.bufferedMutableSharedFlow
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentDatePattern
import io.github.dracula101.jetscan.presentation.platform.feature.document.DocumentTimePattern
import io.github.dracula101.pdf.models.PdfPageSize
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onSubscription

class ConfigDiskSourceImpl(
    sharedPreferences: SharedPreferences,
) : BaseDiskSource(sharedPreferences = sharedPreferences),
    ConfigDiskSource {

    private val onboardingCompleteFlow = bufferedMutableSharedFlow<Boolean>(replay = 1)
    private val isFirstLaunchFlow = bufferedMutableSharedFlow<Boolean>(replay = 1)
    private val showTesterInfoFlow = bufferedMutableSharedFlow<Boolean>(replay = 1)

    private val importExportQualityFlow = bufferedMutableSharedFlow<ImageQuality?>(replay = 1)
    private val importExportQualityDialogFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val allowImageForImportFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val maxDocumentSizeFlow = bufferedMutableSharedFlow<Int?>(replay = 1)
    private val useAppNamingConventionFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val avoidPasswordProtectionFilesFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)

    private val pdfQualityFlow = bufferedMutableSharedFlow<PdfQuality?>(replay = 1)
    private val pdfPageSizeFlow = bufferedMutableSharedFlow<PdfPageSize?>(replay = 1)
    private val hasPdfMarginFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val hasAutoCropFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)

    private val documentPrefixFlow = bufferedMutableSharedFlow<String?>(replay = 1)
    private val documentSuffixFlow = bufferedMutableSharedFlow<String?>(replay = 1)
    private val documentHasDateFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val documentHasTimeFlow = bufferedMutableSharedFlow<Boolean?>(replay = 1)
    private val documentDatePatternFlow = bufferedMutableSharedFlow<DocumentDatePattern?>(replay = 1)
    private val documentTimePatternFlow = bufferedMutableSharedFlow<DocumentTimePattern?>(replay = 1)

    override var onboardingComplete: Boolean
        get() = getBoolean(key = ONBOARDING_COMPLETE_KEY) ?: false
        set(value) {
            putBoolean(key = ONBOARDING_COMPLETE_KEY, value = value)
            onboardingCompleteFlow.tryEmit(value)
        }

    override val onboardingCompleteStateFlow: Flow<Boolean>
        get() = onboardingCompleteFlow.onSubscription { emit(onboardingComplete) }


    override var isFirstLaunch: Boolean
        get() = getBoolean(key = IS_FIRST_LAUNCH_KEY) ?: true
        set(value) {
            putBoolean(key = IS_FIRST_LAUNCH_KEY, value = value)
            isFirstLaunchFlow.tryEmit(value)
        }
    override val isFirstLaunchStateFlow: Flow<Boolean>
        get() = isFirstLaunchFlow.onSubscription { emit(isFirstLaunch) }

    override var showTesterInfo: Boolean
        get() = getBoolean(key = SHOW_TESTER_INFO_KEY) ?: true
        set(value) {
            putBoolean(key = SHOW_TESTER_INFO_KEY, value = value)
            showTesterInfoFlow.tryEmit(value)
        }

    override val showTesterInfoStateFlow: Flow<Boolean>
        get() = showTesterInfoFlow.onSubscription { emit(showTesterInfo) }

    override var importExportQuality: ImageQuality?
        get() = getString(key = IMPORT_EXPORT_QUALITY_KEY)
            ?.let { storedValue -> ImageQuality.entries.firstOrNull { storedValue == it.toFormattedString() } }
        set(value) {
            putString(key = IMPORT_EXPORT_QUALITY_KEY, value = value?.toFormattedString())
            importExportQualityFlow.tryEmit(value)
        }

    override val importExportQualityStateFlow: Flow<ImageQuality?>
        get() = importExportQualityFlow.onSubscription { emit(importExportQuality) }

    override var allowImageForImport: Boolean?
        get() = getBoolean(key = ALLOW_IMAGE_FOR_IMPORT_KEY)
        set(value) {
            putBoolean(key = ALLOW_IMAGE_FOR_IMPORT_KEY, value = value)
            allowImageForImportFlow.tryEmit(value)
        }

    override val showImportQualityDialogStateFlow: Flow<Boolean?>
        get() = importExportQualityDialogFlow.onSubscription { emit(showImportQualityDialog) }

    override var showImportQualityDialog: Boolean?
        get() = getBoolean(key = SHOW_IMPORT_QUALITY_DIALOG_KEY)
        set(value) {
            putBoolean(key = SHOW_IMPORT_QUALITY_DIALOG_KEY, value)
            importExportQualityDialogFlow.tryEmit(value)
        }

    override val allowImageForImportStateFlow: Flow<Boolean?>
        get() = allowImageForImportFlow.onSubscription { emit(allowImageForImport) }

    override var maxDocumentSize: Int?
        get() = getInt(key = MAX_DOCUMENT_SIZE_KEY)
        set(value) {
            putInt(key = MAX_DOCUMENT_SIZE_KEY, value)
            maxDocumentSizeFlow.tryEmit(value)
        }

    override val maxDocumentSizeStateFlow: Flow<Int?>
        get() = maxDocumentSizeFlow.onSubscription { emit(maxDocumentSize) }

    override var useAppNamingConvention: Boolean?
        get() = getBoolean(key = USE_APP_NAMING_CONVENTION_KEY)
        set(value) {
            putBoolean(key = USE_APP_NAMING_CONVENTION_KEY, value)
            useAppNamingConventionFlow.tryEmit(value)
        }

    override val useAppNamingConventionStateFlow: Flow<Boolean?>
        get() = useAppNamingConventionFlow.onSubscription { emit(useAppNamingConvention) }

    override var avoidPasswordProtectionFiles: Boolean?
        get() = getBoolean(key = AVOID_PASSWORD_PROTECTION_FILES_KEY)
        set(value) {
            putBoolean(key = AVOID_PASSWORD_PROTECTION_FILES_KEY, value)
            avoidPasswordProtectionFilesFlow.tryEmit(value)
        }

    override val avoidPasswordProtectionFilesStateFlow: Flow<Boolean?>
        get() = avoidPasswordProtectionFilesFlow.onSubscription { emit(avoidPasswordProtectionFiles) }


    override var pdfQuality: PdfQuality?
        get() = getString(key = PDF_QUALITY_KEY)
            ?.let { storedValue -> PdfQuality.entries.firstOrNull { storedValue == it.name } }
        set(value) {
            putString(key = PDF_QUALITY_KEY, value = value?.name)
            pdfQualityFlow.tryEmit(value)
        }

    override val pdfQualityStateFlow: Flow<PdfQuality?>
        get() = pdfQualityFlow.onSubscription { emit(pdfQuality) }


    override var pdfPageSize: PdfPageSize?
        get() = getString(key = PDF_PAGE_SIZE_KEY)
            ?.let { storedValue -> PdfPageSize.entries.firstOrNull { storedValue == it.name } }
        set(value) {
            putString(key = PDF_PAGE_SIZE_KEY, value = value?.name)
            pdfPageSizeFlow.tryEmit(value)
        }

    override val pdfPageSizeStateFlow: Flow<PdfPageSize?>
        get() = pdfPageSizeFlow.onSubscription { emit(pdfPageSize) }

    override var hasPdfMargin: Boolean?
        get() = getBoolean(key = HAS_PDF_MARGIN_KEY)
        set(value) {
            putBoolean(key = HAS_PDF_MARGIN_KEY, value = value)
            hasPdfMarginFlow.tryEmit(value)
        }

    override val hasPdfMarginStateFlow: Flow<Boolean?>
        get() = hasPdfMarginFlow.onSubscription { emit(hasPdfMargin) }

    override var hasAutoCrop: Boolean?
        get() = getBoolean(key = HAS_AUTO_CROP_KEY)
        set(value) {
            putBoolean(key = HAS_AUTO_CROP_KEY, value = value)
            hasAutoCropFlow.tryEmit(value)
        }

    override val hasAutoCropStateFlow: Flow<Boolean?>
        get() = hasAutoCropFlow.onSubscription { emit(hasAutoCrop) }


    override var documentPrefix: String?
        get() = getString(key = DOCUMENT_PREFIX_KEY)
        set(value) {
            putString(key = DOCUMENT_PREFIX_KEY, value = value)
            documentPrefixFlow.tryEmit(value)
        }

    override val documentPrefixStateFlow: Flow<String?>
        get() = documentPrefixFlow.onSubscription { emit(documentPrefix) }


    override var documentSuffix: String?
        get() = getString(key = DOCUMENT_SUFFIX_KEY)
        set(value) {
            putString(key = DOCUMENT_SUFFIX_KEY, value)
            documentSuffixFlow.tryEmit(value)
        }

    override val documentSuffixStateFlow: Flow<String?>
        get() = documentSuffixFlow.onSubscription { emit(documentSuffix) }


    override var documentHasDate: Boolean?
        get() = getBoolean(key = DOCUMENT_HAS_DATE_KEY)
        set(value) {
            putBoolean(key = DOCUMENT_HAS_DATE_KEY, value = value)
            documentHasDateFlow.tryEmit(value)
        }


    override val documentHasDateStateFlow: Flow<Boolean?>
        get() = documentHasDateFlow.onSubscription { emit(documentHasDate) }


    override var documentHasTime: Boolean?
        get() = getBoolean(key = DOCUMENT_HAS_TIME_KEY)
        set(value) {
            putBoolean(key = DOCUMENT_HAS_TIME_KEY, value = value)
            documentHasTimeFlow.tryEmit(value)
        }

    override val documentHasTimeStateFlow: Flow<Boolean?>
        get() = documentHasTimeFlow.onSubscription { emit(documentHasTime) }


    override var documentDatePattern: DocumentDatePattern?
        get() = getString(key = DOCUMENT_DATE_PATTERN_KEY)
            ?.let { storedValue -> DocumentDatePattern.entries.firstOrNull { storedValue == it.name } }
        set(value) {
            putString(key = DOCUMENT_DATE_PATTERN_KEY, value = value?.name)
            documentDatePatternFlow.tryEmit(value)
        }

    override val documentDatePatternStateFlow: Flow<DocumentDatePattern?>
        get() = documentDatePatternFlow.onSubscription { emit(documentDatePattern) }


    override var documentTimePattern: DocumentTimePattern?
        get() = getString(key = DOCUMENT_TIME_PATTERN_KEY)
            ?.let { storedValue -> DocumentTimePattern.entries.firstOrNull { storedValue == it.name } }
        set(value) {
            putString(key = DOCUMENT_TIME_PATTERN_KEY, value = value?.name)
            documentTimePatternFlow.tryEmit(value)
        }

    override val documentTimePatternStateFlow: Flow<DocumentTimePattern?>
        get() = documentTimePatternFlow.onSubscription { emit(documentTimePattern) }


    override fun clearData() {
        removeWithPrefix(prefix = IMPORT_EXPORT_QUALITY_KEY)
        removeWithPrefix(prefix = ALLOW_IMAGE_FOR_IMPORT_KEY)
        removeWithPrefix(prefix = PDF_QUALITY_KEY)
        removeWithPrefix(prefix = PDF_PAGE_SIZE_KEY)
        removeWithPrefix(prefix = HAS_PDF_MARGIN_KEY)
        removeWithPrefix(prefix = HAS_AUTO_CROP_KEY)
        removeWithPrefix(prefix = DOCUMENT_PREFIX_KEY)
        removeWithPrefix(prefix = DOCUMENT_SUFFIX_KEY)
        removeWithPrefix(prefix = DOCUMENT_HAS_DATE_KEY)
        removeWithPrefix(prefix = DOCUMENT_HAS_TIME_KEY)
        removeWithPrefix(prefix = DOCUMENT_DATE_PATTERN_KEY)
        removeWithPrefix(prefix = DOCUMENT_TIME_PATTERN_KEY)
    }

    companion object {
        private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
        private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
        private const val SHOW_TESTER_INFO_KEY = "show_tester_info"

        private const val IMPORT_EXPORT_QUALITY_KEY = "import_export_quality"
        private const val ALLOW_IMAGE_FOR_IMPORT_KEY = "allow_image_for_import"
        private const val SHOW_IMPORT_QUALITY_DIALOG_KEY = "show_import_quality_dialog"
        private const val MAX_DOCUMENT_SIZE_KEY = "max_document_size"
        private const val USE_APP_NAMING_CONVENTION_KEY = "use_app_naming_convention"
        private const val AVOID_PASSWORD_PROTECTION_FILES_KEY = "avoid_password_protection_files"

        private const val PDF_QUALITY_KEY = "pdf_quality"
        private const val PDF_PAGE_SIZE_KEY = "pdf_page_size"
        private const val HAS_PDF_MARGIN_KEY = "has_pdf_margin"
        private const val HAS_AUTO_CROP_KEY = "has_auto_crop"

        private const val DOCUMENT_PREFIX_KEY = "document_prefix"
        private const val DOCUMENT_SUFFIX_KEY = "document_suffix"
        private const val DOCUMENT_HAS_DATE_KEY = "document_has_date"
        private const val DOCUMENT_HAS_TIME_KEY = "document_has_time"
        private const val DOCUMENT_DATE_PATTERN_KEY = "document_date_pattern"
        private const val DOCUMENT_TIME_PATTERN_KEY = "document_time_pattern"
    }
}