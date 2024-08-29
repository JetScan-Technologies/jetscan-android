package io.github.dracula101.jetscan.presentation.features.document.scanner

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Parcelable
import androidx.annotation.FloatRange
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.data.document.manager.file.FileManager
import io.github.dracula101.jetscan.data.document.manager.file.ScannedDocDirectory
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManager
import io.github.dracula101.jetscan.data.document.models.doc.DocQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.jetscan.data.platform.utils.readableSize
import io.github.dracula101.jetscan.data.platform.utils.rotate
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageColorAdjustment
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageOrientation
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.imageproxy.BarcodeAnalyzer
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraAspectRatio
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraFacingMode
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.FlashMode
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.GridMode
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject


const val SCANNER_STATE = "scanner_state"

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val mainExecutor: Executor,
    private val openCvManager: OpenCvManager,
    private val fileManager: FileManager,
    private val pdfManager: PdfManager,
    private val documentRepository: DocumentRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<ScannerState, Unit, ScannerAction>(
    initialState = savedStateHandle[SCANNER_STATE] ?: ScannerState()
) {
    // ================== Camera Controller StateFlow ======================
    private val _cameraController = MutableStateFlow<CameraController?>(null)
    private val cameraController: CameraController?
        get() = _cameraController.value

    val imageProcessingManager: OpenCvManager = openCvManager

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


    // ================== Cached Filter Bitmaps ======================
    private val _cachedFilterBitmaps =  MutableStateFlow<Map<Int, List<Bitmap>?>>(emptyMap())
    fun cachedFilterBitmaps(documentIndex: Int): List<Bitmap>? = _cachedFilterBitmaps.value.get(documentIndex)

    private val rotationListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val zRotationDegrees = Math.toDegrees(orientation[0].toDouble() + Math.PI).toFloat() + 90
            val rotationValue = when {
                zRotationDegrees < 45 -> 0f
                zRotationDegrees < 135 -> 90f
                zRotationDegrees < 225 -> 180f
                zRotationDegrees < 315 -> 270f
                else -> 0f
            }
            if (rotationValue != state.cameraRotationValue && state.scannerView == ScannerView.CAMERA) {
                mutableStateFlow.update {
                    it.copy(cameraRotationValue = rotationValue)
                }
            }
        }
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    init {
        sensorManager.registerListener(rotationListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun handleAction(action: ScannerAction) {
        when (action) {
            is ScannerAction.Ui.ChangeCameraFacingMode -> handleCameraFacingModeChange()
            is ScannerAction.Ui.ChangeFlashLightMode -> handleFlashLightModeChange()
            is ScannerAction.Ui.ChangeGridMode -> handleGridModeChange()
            is ScannerAction.Ui.ChangeDocumentType -> handleDocumentTypeChange(action.documentType)
            is ScannerAction.Ui.OnCapturePhoto -> handleCapturePhoto()
            is ScannerAction.Ui.ChangeScannerView -> handleScannerView(action.scannerView)
            is ScannerAction.Ui.FirstCropDocument -> handleFirstCropDocument(action.scannedDocument)
            is ScannerAction.Ui.ChangeEditDocumentIndex -> handleEditDocumentIndex(action.index)
            is ScannerAction.Ui.OnBackPress -> handleBackPress(action.navigateBack)
            is ScannerAction.Ui.OnDismissBarCode -> handleDismissBarcode()
            is ScannerAction.Ui.OnSaveDocument -> handleStartSavingDocument()

            is ScannerAction.OnCameraInitialized -> _cameraController.value =
                action.cameraController

            is ScannerAction.EditAction.DocumentChangeName -> handleDocumentNameChange(action.name)
            is ScannerAction.EditAction.CropDocument -> handleCropDocument(action.index)
            is ScannerAction.EditAction.RetakeDocument -> handleRetakeDocument(action.index)
            is ScannerAction.EditAction.RotateDocument -> handleRotateDocument(action.index)
            is ScannerAction.EditAction.ApplyFilter -> handleApplyFilter(state.currentDocumentIndex, action.filter)
            is ScannerAction.EditAction.ChangeColorAdjustTab -> handleColorAdjustTabChange(action.tab)
            is ScannerAction.EditAction.ChangeColorAdjustment -> handleColorAdjustmentChange(action.brightness, action.contrast, action.saturation)

            is ScannerAction.Internal.DeleteDocument -> handleDeleteDocument(action.index)
            is ScannerAction.Internal.UpdateProgress -> handleUpdateProgress(action.progress)
            is ScannerAction.Internal.SaveDocumentToDB -> handleSaveDocument(action.scannedDocDir)

            is ScannerAction.Alert.CameraModeChangedAlert -> handleCameraModeChangedAlert(action)
            is ScannerAction.Alert.RemoveCameraModeAlert -> handleRemoveCameraModeAlert()
            is ScannerAction.Alert.DeleteImageAlert -> handleDeleteImageAlert()
            is ScannerAction.Alert.ShowFilterImageAlert -> handleShowFilterImageAlert()
            is ScannerAction.Alert.ShowExitCameraAlert -> handleShowCameraExitAlert()
            is ScannerAction.Alert.ShowUnsavedDocumentAlert -> handleShowUnsavedDocumentAlert(action.nextTab)
            is ScannerAction.Alert.ShowSavingDocumentAlert -> handleSavingDocument()
            is ScannerAction.Alert.DismissAlert -> handleDismissAlert(action)
        }
    }

    private fun handleCapturePhoto() {
        mutableStateFlow.update { it.copy(isCapturingPhoto = true) }
        cameraController?.takePicture(
            mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onPhotoClickedSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Timber.e(exception)
                    mutableStateFlow.update {
                        it.copy(
                            isCapturingPhoto = false,
                            snackbarState = SnackbarState.ShowError(
                                title = "Capture Error",
                                message = exception.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
            }
        )
    }

    private fun onPhotoClickedSuccess(image: ImageProxy) {
        viewModelScope.launch {
            val bitmap = image.use { image.toBitmap() }
            val scannedImage = CameraScannedImage.fromBitmap(bitmap)
            val croppedBitmap = openCvManager.cropDocument(bitmap, scannedImage.cropCoords)
            val scannedDocument = scannedImage.copy(croppedImage = croppedBitmap)
            val scannedDocuments = stateFlow.value.scannedDocuments.toMutableList()
            val retakeDocumentIndex = stateFlow.value.retakeDocumentIndex ?: scannedDocuments.size
            scannedDocuments.add(retakeDocumentIndex, scannedDocument)
            mutableStateFlow.update {
                it.copy(
                    isCapturingPhoto = false,
                    scannedDocuments = scannedDocuments
                )
            }
            Timber.i("Captured Image: ${bitmap.width}x${bitmap.height} - ${bitmap.readableSize}")
            trySendAction(ScannerAction.Ui.ChangeScannerView(ScannerView.CROP_DOCUMENT))
            image.close()
        }
    }

    private fun handleCameraFacingModeChange() {
        val isFrontFacing = stateFlow.value.cameraFacing == CameraFacingMode.FRONT
        changeCameraFacing(!isFrontFacing) {
            mutableStateFlow.update {
                it.copy(cameraFacing = if (isFrontFacing) CameraFacingMode.BACK else CameraFacingMode.FRONT)
            }
        }
        trySendAction(ScannerAction.Alert.CameraModeChangedAlert(cameraFacing = stateFlow.value.cameraFacing))
    }

    private fun changeCameraFacing(isFrontFacing: Boolean, onSuccess: () -> Unit) {
        try {
            val cameraSelector = if (isFrontFacing) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            cameraController?.cameraSelector = cameraSelector
            onSuccess()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun handleFlashLightModeChange() {
        val flashMode = listOf(
            FlashMode.ON,
            FlashMode.OFF,
            FlashMode.AUTO,
            FlashMode.TORCH
        )
        val currentIndex = flashMode.indexOf(stateFlow.value.flashMode)
        val newIndex = (currentIndex + 1) % flashMode.size
        val newFlashMode = flashMode[newIndex]
        changeFlashMode(newFlashMode) {
            mutableStateFlow.update {
                it.copy(flashMode = newFlashMode)
            }
        }
        trySendAction(ScannerAction.Alert.CameraModeChangedAlert(flashMode = newFlashMode))
    }

    private fun changeFlashMode(flashMode: FlashMode, onSuccess: () -> Unit) {
        try {
            val imageCaptureMode = when (flashMode) {
                FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF
            }
            cameraController?.imageCaptureFlashMode = imageCaptureMode
            val enableTorch = when (flashMode) {
                FlashMode.OFF, FlashMode.ON, FlashMode.AUTO -> false
                FlashMode.TORCH -> true
            }
            cameraController?.enableTorch(enableTorch)
            onSuccess()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun handleGridModeChange() {
        val gridMode = when (state.gridMode) {
            GridMode.OFF -> GridMode.ON
            GridMode.ON -> GridMode.OFF
        }
        mutableStateFlow.update {
            it.copy(gridMode = gridMode)
        }
        trySendAction(ScannerAction.Alert.CameraModeChangedAlert(gridMode = gridMode))
    }

    private fun handleScannerView(scannerView: ScannerView) {
        mutableStateFlow.update {
            it.copy(scannerView = scannerView)
        }
    }

    private fun handleDocumentTypeChange(documentType: DocumentType) {
        mutableStateFlow.update {
            it.copy(documentType = documentType)
        }
        if (documentType == DocumentType.BAR_CODE || documentType == DocumentType.QR_CODE) {
            startBarCodeMonitoring()
        } else {
            turnOffMonitoring()
        }
    }

    private fun turnOffMonitoring() {
        cameraController?.clearImageAnalysisAnalyzer()
    }

    private fun startBarCodeMonitoring() {
        try {
            Timber.i("Monitoring for Bar code")
            cameraController?.clearImageAnalysisAnalyzer()
            cameraController?.setImageAnalysisAnalyzer(
                mainExecutor,
                BarcodeAnalyzer(
                    onCodeDetected = { qrCode ->
                        if (!state.barCode?.rawBytes.contentEquals(qrCode.rawBytes)) {
                            Timber.i("Bar code detected: ${qrCode.displayValue}")
                            mutableStateFlow.update {
                                it.copy(barCode = qrCode)
                            }
                        }
                    }
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    private fun handleCameraModeChangedAlert(action: ScannerAction.Alert.CameraModeChangedAlert) {
        val flashMode = action.flashMode
        val gridMode = action.gridMode
        val cameraFacing = action.cameraFacing
        val alertMessage = when {
            flashMode != null -> when (flashMode) {
                FlashMode.ON -> "Flash On"
                FlashMode.OFF -> "Flash Off"
                FlashMode.AUTO -> "Flash Auto"
                FlashMode.TORCH -> "Torch"
            }

            gridMode != null -> when (gridMode) {
                GridMode.ON -> "Grid On"
                GridMode.OFF -> "Grid Off"
            }

            cameraFacing != null -> when (cameraFacing) {
                CameraFacingMode.FRONT -> "Front Camera"
                CameraFacingMode.BACK -> "Back Camera"
            }

            else -> null
        }
        mutableStateFlow.update {
            it.copy(cameraModeChangedAlert = alertMessage)
        }
        viewModelScope.launch {
            delay(1000)
            trySendAction(ScannerAction.Alert.RemoveCameraModeAlert)
        }
    }

    private fun handleRemoveCameraModeAlert() {
        mutableStateFlow.update {
            it.copy(cameraModeChangedAlert = null)
        }
    }

    private fun handleDeleteImageAlert() {
        mutableStateFlow.update {
            it.copy(
                dialogState = ScannerDialogState.DeleteImage
            )
        }
    }

    private fun handleShowFilterImageAlert() {
        mutableStateFlow.update {
            it.copy(
                dialogState = ScannerDialogState.FilterImage
            )
        }
    }

    private fun handleShowCameraExitAlert() {
        mutableStateFlow.update {
            it.copy(
                dialogState = ScannerDialogState.ExitScanner
            )
        }
    }

    private fun handleShowUnsavedDocumentAlert(
        nextTab: DocumentType,
        onChangeTab: () -> Unit = {}
    ) {
        mutableStateFlow.update {
            it.copy(
                dialogState = ScannerDialogState.UnsavedDocument(
                    nextTab = nextTab,
                    onChangeTab = onChangeTab
                )
            )
        }
    }

    private fun handleSavingDocument() {
        mutableStateFlow.update {
            it.copy(
                dialogState = ScannerDialogState.SaveDocument
            )
        }
    }

    private fun handleDismissAlert(action: ScannerAction.Alert.DismissAlert) {
        mutableStateFlow.update {
            it.copy(
                dialogState = if (action.dialog) null else it.dialogState,
                snackbarState = if (action.snackbar) null else it.snackbarState
            )
        }
    }

    private fun handleDeleteDocument(index: Int) {
        val updatedDocuments = stateFlow.value.scannedDocuments.filterIndexed { i, _ -> i != index }
        mutableStateFlow.update {
            it.copy(scannedDocuments = updatedDocuments)
        }
        trySendAction(ScannerAction.Alert.DismissAlert(dialog = true))
    }

    private fun handleUpdateProgress(progress: Float) {
        Timber.i("Progress: $progress")
        mutableStateFlow.update {
            it.copy(
                savingDocState = it.savingDocState?.copy(
                    currentProgress = progress
                )
            )
        }
    }

    private fun handleSaveDocument(scannedDocDir: ScannedDocDirectory){
        viewModelScope.launch(Dispatchers.IO) {
            var fileLength = 0L
            for (file in scannedDocDir.scannedImageDirectory.listFiles() ?: emptyArray()) {
                fileLength += file.length()
            }
            val fileName = state.documentName
            val scannedImages = scannedDocDir.scannedImageDirectory.listFiles()?.map {
                Timber.i("Scanned Image: ${it.name}, Size: ${it.length().bytesToReadableSize()}")
                ScannedImage.fromFile(it)
            } ?: emptyList()
            val previewImageUri = scannedDocDir.previewImage.listFiles()?.first()?.toUri() ?: scannedImages.first().scannedUri
            val originalFile = File(scannedDocDir.mainDirectory, "Original.pdf")
            pdfManager.saveToPdf(scannedImages.map { File(it.scannedUri.path!!) }, originalFile, DocQuality.PPI_72)
            if (scannedImages.isEmpty()) throw Exception("No images found")
            val scannedDoc = Document(
                size = originalFile.length(),
                uri = originalFile.toUri(),
                dateCreated = System.currentTimeMillis(),
                name = fileName,
                previewImageUri = previewImageUri,
                scannedImages = scannedImages
            )
            val isAdded = documentRepository.addDocument(scannedDoc)
            Timber.i("Document Added: $isAdded")
            if (!isAdded) throw Exception("Error saving document")
            mutableStateFlow.update {
                it.copy(
                    snackbarState = SnackbarState.ShowSuccess(
                        title = "Document Saved",
                    ),
                )
            }
            trySendAction(ScannerAction.Alert.DismissAlert(dialog = true,snackbar = true))
            delay(500)
            mutableStateFlow.update { it.copy(isDocumentSaved = true) }
        }.runCatching{
            this
        }.getOrElse {
            Timber.e(it)
            mutableStateFlow.update { state->
                state.copy(
                    savingDocState = SavingDocumentState(
                        hasError = it as Exception
                    )
                )
            }
            trySendAction(ScannerAction.Alert.DismissAlert(dialog = true,snackbar = true))
        }
    }

    private fun handleDocumentNameChange(name: String) {
        mutableStateFlow.update {
            it.copy(documentName = name)
        }
    }

    private fun handleFirstCropDocument(scannedDocument: CameraScannedImage) {
        val croppedBitmap =
            openCvManager.cropDocument(scannedDocument.originalImage, scannedDocument.cropCoords)
        val updatedDocuments = stateFlow.value.scannedDocuments.toMutableList()
        if (state.retakeDocumentIndex != null) {
            updatedDocuments[state.retakeDocumentIndex!!] =
                scannedDocument.copy(croppedImage = croppedBitmap)
        } else {
            updatedDocuments.removeLastOrNull()
            updatedDocuments.add(scannedDocument.copy(croppedImage = croppedBitmap))
        }
        mutableStateFlow.update {
            it.copy(
                scannedDocuments = updatedDocuments,
                scannerView = ScannerView.CAMERA,
                cropDocumentIndex = updatedDocuments.size - 1,
                retakeDocumentIndex = null,
            )
        }
    }

    private fun handleCropDocument(index: Int) {
        mutableStateFlow.update {
            it.copy(
                scannerView = ScannerView.CROP_DOCUMENT,
                cropDocumentIndex = index
            )
        }
    }

    private fun handleRetakeDocument(index: Int) {
        val updatedDocuments = stateFlow.value.scannedDocuments.filterIndexed { i, _ -> i != index }
        mutableStateFlow.update {
            it.copy(
                scannedDocuments = updatedDocuments,
                retakeDocumentIndex = index,
                scannerView = ScannerView.CAMERA
            )
        }
    }

    private fun handleRotateDocument(index: Int) {
        val updatedDocuments = stateFlow.value.scannedDocuments.toMutableList()
        val scannedDocument = updatedDocuments[index]
        if (scannedDocument.croppedImage == null) return
        val rotation = when (scannedDocument.imageEffect.orientation) {
            ImageOrientation.ROTATION_0 -> ImageOrientation.ROTATION_90
            ImageOrientation.ROTATION_90 -> ImageOrientation.ROTATION_180
            ImageOrientation.ROTATION_180 -> ImageOrientation.ROTATION_270
            ImageOrientation.ROTATION_270 -> ImageOrientation.ROTATION_0
        }
        updatedDocuments[index] = scannedDocument.copy(
            imageEffect = scannedDocument.imageEffect.copy(
                orientation = rotation
            ),
        )
        mutableStateFlow.update {
            it.copy(
                scannedDocuments = updatedDocuments
            )
        }
    }

    private fun handleApplyFilter(index: Int, filter: ImageFilter) {
        val updatedDocuments = stateFlow.value.scannedDocuments.toMutableList()
        if (updatedDocuments[index].croppedImage == null) return
        val filteredImage =
            openCvManager.applyFilter(updatedDocuments[index].croppedImage!!, filter)
        val scannedDocument = updatedDocuments[index].copy(
            imageEffect = updatedDocuments[index].imageEffect.copy(
                imageFilter = filter
            ),
            filteredImage = filteredImage
        )
        updatedDocuments[index] = scannedDocument
        mutableStateFlow.update {
            it.copy(
                scannedDocuments = updatedDocuments
            )
        }
    }

    private fun handleColorAdjustTabChange(tab: ColorAdjustTab) {
        mutableStateFlow.update {
            it.copy(
                selectedColorAdjustTab = tab
            )
        }
    }

    private fun handleColorAdjustmentChange(brightness: Float?, contrast: Float?, saturation: Float?) {
        val currentDocument = state.scannedDocuments[state.currentDocumentIndex]
        val updatedDocuments = state.scannedDocuments.toMutableList()
        val updatedColorAdjustment = ImageColorAdjustment(
            brightness = brightness ?: currentDocument.imageEffect.colorAdjustment.brightness,
            contrast = contrast ?: currentDocument.imageEffect.colorAdjustment.contrast,
            saturation = saturation ?: currentDocument.imageEffect.colorAdjustment.saturation
        )
        if (currentDocument.croppedImage == null) return
        val filteredBitmap = openCvManager.applyColorAdjustment(
            currentDocument.croppedImage,
            updatedColorAdjustment.brightness,
            updatedColorAdjustment.contrast,
            updatedColorAdjustment.saturation
        )
        val updatedDocument = currentDocument.copy(
            filteredImage = filteredBitmap,
            imageEffect = currentDocument.imageEffect.copy(
                colorAdjustment = updatedColorAdjustment
            )
        )
        updatedDocuments[state.currentDocumentIndex] = updatedDocument
        mutableStateFlow.update {
            it.copy(scannedDocuments = updatedDocuments)
        }

    }

    private fun handleEditDocumentIndex(index: Int) {
        mutableStateFlow.update {
            it.copy(
                currentDocumentIndex = index
            )
        }
    }

    private fun handleBackPress(navigateBack: () -> Unit) {
        if (state.scannedDocuments.isEmpty()) {
            navigateBack()
        } else {
            trySendAction(ScannerAction.Alert.ShowExitCameraAlert)
        }
    }

    private fun handleDismissBarcode() {
        mutableStateFlow.update {
            it.copy(barCode = null)
        }
    }

    private fun handleStartSavingDocument() {
        Timber.i("Started Saving Document")
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            mutableStateFlow.update {
                it.copy(
                    dialogState = ScannerDialogState.SaveDocument,
                    savingDocState = SavingDocumentState(
                        currentProgress = 0.0f
                    ),
                )
            }
            val bitmaps = state.scannedDocuments.map { (it.filteredImage ?: it.croppedImage!!).rotate(it.imageEffect.orientation.toDegrees()) }
            val imageQuality : Int = when(bitmaps.size){
                in 1..3 -> 95
                in 4..6 -> 90
                in 7..10 -> 85
                in 11..15 -> 80
                in 15..20 -> 75
                in 21..40 -> 60
                else -> 50
            }
            fileManager.addScannedDocumentFromScanner(
                bitmaps = bitmaps,
                fileName = state.documentName,
                imageQuality = imageQuality,
            ) { currentProgress: Float, totalProgress: Int ->
                //trySendAction(ScannerAction.Internal.UpdateProgress(currentProgress / totalProgress))
            }.runCatching {
                this
            }.getOrElse {
                mutableStateFlow.update {
                    it.copy(
                        savingDocState = SavingDocumentState(
                            hasError = Exception("Error saving document")
                        )
                    )
                }
                return@launch
            }.also { taskFile ->
                when (taskFile) {
                    is Task.Success -> {
                        mutableStateFlow.update {
                            it.copy(
                                savingDocState = SavingDocumentState(
                                    saved = true,
                                    currentProgress = 1f,
                                    hasError = null
                                )
                            )
                        }
                        trySendAction(ScannerAction.Internal.SaveDocumentToDB(taskFile.data))
                    }

                    is Task.Error -> {
                        mutableStateFlow.update {
                            it.copy(
                                savingDocState = SavingDocumentState(
                                    hasError = taskFile.error as Exception
                                )
                            )
                        }
                    }

                    is Task.Cancelled, is Task.Idle -> {}
                }
            }
        }
    }

    fun cacheFilterBitmaps(bitmaps: List<Bitmap>) {
        if (_cachedFilterBitmaps.value.get(state.currentDocumentIndex) == null) {
            _cachedFilterBitmaps.value = _cachedFilterBitmaps.value.toMutableMap().apply {
                put(state.currentDocumentIndex, bitmaps)
            }

        }
    }

    override fun onCleared() {
        _cachedFilterBitmaps.value.forEach{ (_, bitmaps) ->
            bitmaps?.forEach { it.recycle() }
        }
        _cameraController.value = null
        savedStateHandle[SCANNER_STATE] = stateFlow.value
        sensorManager.unregisterListener(rotationListener)
        super.onCleared()
    }

}

@Parcelize
data class ScannerState(
    val cameraAspectRatio: CameraAspectRatio = CameraAspectRatio.RATIO_4_3,
    val flashMode: FlashMode = FlashMode.OFF,
    val gridMode: GridMode = GridMode.OFF,
    val cameraFacing: CameraFacingMode = CameraFacingMode.BACK,
    val documentType: DocumentType = DocumentType.DOCUMENT,
    val scannedDocuments: List<CameraScannedImage> = emptyList(),
    val snackbarAlert: String? = null,
    val cameraModeChangedAlert: String? = null,
    val isCapturingPhoto: Boolean = false,
    val dialogState: ScannerDialogState? = null,
    val snackbarState: SnackbarState? = null,
    val retakeDocumentIndex: Int? = null,
    @IgnoredOnParcel val barCode: Barcode? = null,
    val savingDocState: SavingDocumentState? = null,
    val isDocumentSaved: Boolean = false,
    val cameraRotationValue: Float = 0f,
    // Crop Screen values
    val cropDocumentIndex: Int = 0,

    // Edit Screen values
    val scannerView: ScannerView = ScannerView.CAMERA,
    val currentDocumentIndex: Int = 0,
    val documentName: String = "Scan - " + SimpleDateFormat(
        "dd/MM/yy h:mm a",
        Locale.getDefault()
    ).format(System.currentTimeMillis()),
    val selectedColorAdjustTab: ColorAdjustTab = ColorAdjustTab.SATURATION,
) : Parcelable

@Parcelize
sealed class ScannerDialogState : Parcelable {
    data object DeleteImage : ScannerDialogState()
    data object FilterImage : ScannerDialogState()
    data object ExitScanner : ScannerDialogState()
    data class UnsavedDocument(
        val nextTab: DocumentType,
        val onChangeTab: () -> Unit = {}
    ) : ScannerDialogState()

    data object SaveDocument : ScannerDialogState()
}

sealed class ScannerAction {

    @Parcelize
    sealed class Ui : ScannerAction(), Parcelable {
        data object ChangeFlashLightMode : Ui()
        data object ChangeGridMode : Ui()
        data object ChangeCameraFacingMode : Ui()
        data class ChangeDocumentType(val documentType: DocumentType) : Ui()
        data object OnCapturePhoto : Ui()
        data class ChangeScannerView(val scannerView: ScannerView) : Ui()
        data class FirstCropDocument(val scannedDocument: CameraScannedImage) : Ui()
        data class ChangeEditDocumentIndex(val index: Int) : Ui()
        data class OnBackPress(val navigateBack: () -> Unit) : Ui()
        data object OnDismissBarCode : Ui()
        data object OnSaveDocument : Ui()
    }

    data class OnCameraInitialized(val cameraController: CameraController) : ScannerAction()

    @Parcelize
    sealed class EditAction : ScannerAction(), Parcelable {
        data class DocumentChangeName(val name: String) : Ui()
        data class CropDocument(val index: Int) : Ui()
        data class RetakeDocument(val index: Int) : Ui()
        data class RotateDocument(val index: Int) : Ui()
        data class ApplyFilter(val filter: ImageFilter) : Ui()
        data class ChangeColorAdjustTab(val tab: ColorAdjustTab) : Ui()
        data class ChangeColorAdjustment(val saturation: Float? = null, val brightness: Float? = null, val contrast: Float? = null) : Ui()
    }

    @Parcelize
    sealed class Internal : ScannerAction(), Parcelable {
        data class DeleteDocument(val index: Int) : Internal()
        data class UpdateProgress(val progress: Float) : Internal()
        data class SaveDocumentToDB(val scannedDocDir: ScannedDocDirectory): Internal()
    }

    @Parcelize
    sealed class Alert : ScannerAction(), Parcelable {
        data class CameraModeChangedAlert(
            val flashMode: FlashMode? = null,
            val gridMode: GridMode? = null,
            val cameraFacing: CameraFacingMode? = null,
        ) : Alert()

        data object DeleteImageAlert : Alert()
        data object RemoveCameraModeAlert : Alert()
        data object ShowFilterImageAlert : Alert()
        data object ShowExitCameraAlert : Alert()
        data class ShowUnsavedDocumentAlert(
            val nextTab: DocumentType,
            val onChangeTab: () -> Unit = {}
        ) : Alert()
        data object ShowSavingDocumentAlert : Alert()

        data class DismissAlert(val dialog: Boolean = false, val snackbar: Boolean = false) : Alert()
    }
}

enum class ScannerView {
    CAMERA,
    EDIT_DOCUMENT,
    CROP_DOCUMENT,
}

enum class ColorAdjustTab {
    BRIGHTNESS,
    CONTRAST,
    SATURATION
}

@Parcelize
data class SavingDocumentState(
    @FloatRange(from = 0.0, to = 1.0)
    val currentProgress: Float = 0f,
    val saved: Boolean = false,
    val hasError: Exception? = null
) : Parcelable
