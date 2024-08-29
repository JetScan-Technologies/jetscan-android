package io.github.dracula101.jetscan.presentation.features.document.scanner

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.BarCodeOverlay
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.CameraFacingArgs
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.CameraFunctions
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.CameraPreviewContent
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.DocumentTypeSelector
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.FlashModeArgs
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.GridModeArgs
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.NoPermissionView
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.QrCodeOverlay
import io.github.dracula101.jetscan.presentation.features.document.scanner.components.ScannerTopAppBar
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAlertSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.dialog.IconAlertDialog
import io.github.dracula101.jetscan.presentation.platform.component.extensions.gradientContainer
import io.github.dracula101.jetscan.presentation.platform.component.loader.AnimatedLoader
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showErrorSnackBar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showSuccessSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showWarningSnackbar
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toPath
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraAspectRatio
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraFacingMode
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.GridMode
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    startingTab: DocumentType,
) {
    val isPermissionGranted = remember { mutableStateOf<Boolean?>(null) }
    val isCameraPermissionGranted =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA) { isGranted ->
            isPermissionGranted.value = isGranted
        }
    when (isCameraPermissionGranted.status) {
        is PermissionStatus.Denied -> NoPermissionView(
            modifier = modifier,
            onPermissionRequested = {
                isCameraPermissionGranted.launchPermissionRequest()
            },
            onNavigateBack = onNavigateBack,
            isPermissionDeclined = isPermissionGranted.value
        )

        is PermissionStatus.Granted -> ScannerScreenView(
            onNavigateBack = onNavigateBack,
            startingTab = startingTab
        )
    }
}

@Composable
fun ScannerScreenView(
    onNavigateBack: () -> Unit,
    startingTab: DocumentType,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val documentCropCoords = remember { mutableStateOf<ImageCropCoords?>(null) }
    when (state.value.isDocumentSaved) {
        true -> { onNavigateBack() }
        else -> {}
    }
    LaunchedEffect(Unit) {
        viewModel.trySendAction(ScannerAction.Ui.ChangeDocumentType(startingTab))
    }

    BackHandler {
        when (state.value.scannerView) {
            ScannerView.CAMERA -> {
                viewModel.trySendAction(ScannerAction.Ui.OnBackPress(onNavigateBack))
            }
            ScannerView.EDIT_DOCUMENT, ScannerView.CROP_DOCUMENT -> {
                viewModel.trySendAction(
                    ScannerAction.Ui.ChangeScannerView(
                        ScannerView.CAMERA
                    )
                )
            }
        }
    }

    ScannerViewDialog(
        state = state,
        cachedFilterBitmaps = viewModel.cachedFilterBitmaps(state.value.currentDocumentIndex),
        onCacheBitmap = { bitmaps ->
            viewModel.cacheFilterBitmaps(bitmaps)
        },
        dismissAlert = {
            viewModel.trySendAction(ScannerAction.Alert.DismissAlert(dialog = true))
        },
        deleteDocument = {
            viewModel.trySendAction(ScannerAction.Internal.DeleteDocument(state.value.currentDocumentIndex))
        },
        applyFilterProcess = { bitmap ->
            viewModel.imageProcessingManager.applyFilters(bitmap)
        },
        applyFilter = { filter ->
            viewModel.trySendAction(ScannerAction.EditAction.ApplyFilter(filter))
        },
        onNavigateBack = onNavigateBack
    )
    ShowBarCodeBottomSheet(
        barCode = state.value.barCode,
        onDismiss = {
            viewModel.trySendAction(ScannerAction.Ui.OnDismissBarCode)
        }
    )

    when (state.value.scannerView) {
        ScannerView.CAMERA -> {
            ScannerView(
                onNavigateBack = onNavigateBack,
                state = state,
                viewModel = viewModel,
                previewAspectRatio = state.value.cameraAspectRatio,
                documentCropCoords = documentCropCoords
            )
        }

        ScannerView.EDIT_DOCUMENT -> {
            ScannerEditView(
                documents = state.value.scannedDocuments,
                state = state.value,
                viewModel = viewModel,
                initialDocumentIndex = state.value.cropDocumentIndex,
                backToCamera = {
                    viewModel.trySendAction(ScannerAction.Ui.ChangeScannerView(ScannerView.CAMERA))
                },
            )
        }

        ScannerView.CROP_DOCUMENT -> {
            ScannerCropView(
                state = state.value,
                scannedDocument = state.value.scannedDocuments[
                    state.value.retakeDocumentIndex ?: (state.value.scannedDocuments.size - 1)
                ],
                viewModel = viewModel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannerView(
    onNavigateBack: () -> Unit,
    state: State<ScannerState>,
    viewModel: ScannerViewModel,
    previewAspectRatio: CameraAspectRatio,
    documentCropCoords: MutableState<ImageCropCoords?>,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    state.value.snackbarState?.let { snackbarState ->
        ScannerAlertSnackbar(
            snackbarHostState = snackbarHostState,
            snackbarState = snackbarState,
            onDismiss = {
                viewModel.trySendAction(ScannerAction.Alert.DismissAlert(snackbar = true))
            }
        )
    }
    JetScanScaffold(
        modifier = Modifier,
        snackbarHostState = snackbarHostState,
    ) { padding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top,
        ) {
            ScannerTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(80.dp),
                onNavigateBack = {
                    viewModel.trySendAction(
                        ScannerAction.Ui.OnBackPress(
                            onNavigateBack
                        )
                    )
                },
                flashModeArgs = FlashModeArgs(
                    flashMode = state.value.flashMode,
                    onFlashModeChanged = {
                        viewModel.trySendAction(ScannerAction.Ui.ChangeFlashLightMode)
                    },
                ),
                gridModeArgs = GridModeArgs(
                    gridModeOn = state.value.gridMode == GridMode.ON,
                    onGridModeChanged = {
                        viewModel.trySendAction(ScannerAction.Ui.ChangeGridMode)
                    }
                ),
                cameraFacingArgs = CameraFacingArgs(
                    cameraFacingBack = state.value.cameraFacing == CameraFacingMode.BACK,
                    onCameraFacingChanged = {
                        viewModel.trySendAction(ScannerAction.Ui.ChangeCameraFacingMode)
                    }
                )
            )
            Box(
                modifier = Modifier
                    .aspectRatio(previewAspectRatio.toFloat())
                    .clipToBounds()
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CameraPreviewContent(
                    onCameraInitialized = { controller ->
                        viewModel.trySendAction(ScannerAction.OnCameraInitialized(controller))
                    },
                    onImageAnalysisInitialized = { controller, size ->
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    previewSize.value = size
                                    controller.setImageAnalysisAnalyzer(
                                        context.mainExecutor,
                                        DocumentAnalyzer(
                                            openCvManager = viewModel.imageProcessingManager,
                                            onDocumentDetected = { imageCropCoords ->
                                                documentCropCoords.value = imageCropCoords
                                            },
                                            imagePreviewSize = size,
                                            onPreviewBitmapReady = { bitmap1, bitmap2 ->
                                                originalBitmap.value = bitmap1
                                                previewBitmap.value = bitmap2
                                            }
                                        )
                                    )
                                }*/
                    },
                    gridStatus = state.value.gridMode == GridMode.ON,
                    previewAspectRatio = state.value.cameraAspectRatio,
                    modifier = Modifier
                        .aspectRatio(previewAspectRatio.toFloat())
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .drawWithContent {
                            if (documentCropCoords.value != null) {
                                val path = documentCropCoords.value!!.toPath()
                                drawPath(path = path, alpha = 0.3f, color = Color.White)
                                // draw a outline of the crop area
                                drawPath(path = path, color = Color.White, style = Stroke(4f))
                            }
                        }
                )
                when(state.value.documentType){
                    DocumentType.BAR_CODE -> BarCodeOverlay()
                    DocumentType.QR_CODE -> QrCodeOverlay()
                    else -> {}
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                ) {
                    AnimatedContent(
                        targetState = state.value.cameraModeChangedAlert,
                        label = "Camera mode alert",
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) { cameraModeChangedAlert ->
                        if (cameraModeChangedAlert != null) {
                            Surface(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small),
                            ) {
                                Text(
                                    text = cameraModeChangedAlert,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                    CameraOrientationAlert(state = state)
                }

            }
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                DocumentTypeSelector(
                    modifier = Modifier.fillMaxHeight(0.35f),
                    documentType = state.value.documentType,
                    onDocumentTypeChanged = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.trySendAction(ScannerAction.Ui.ChangeDocumentType(it))
                    },
                )
                CameraFunctions(
                    modifier = Modifier.fillMaxHeight(0.8f),
                    state = state.value,
                    previewAspectRatio = previewAspectRatio.toFloat(),
                    document = if (state.value.scannedDocuments.isNotEmpty()) state.value.scannedDocuments.last() else null,
                    isCapturingPhoto = state.value.isCapturingPhoto,
                    onCapturePhoto = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.trySendAction(ScannerAction.Ui.OnCapturePhoto)
                    },
                    onDocumentClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.trySendAction(ScannerAction.Ui.ChangeScannerView(ScannerView.EDIT_DOCUMENT))
                    },
                )
            }
        }
    }
}

enum class OrientationTextPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT;

    fun toAlignment(): Alignment {
        return when (this) {
            TOP -> Alignment.TopCenter
            BOTTOM -> Alignment.BottomCenter
            LEFT -> Alignment.CenterStart
            RIGHT -> Alignment.CenterEnd
        }
    }

}

@Composable
fun BoxScope.CameraOrientationAlert(
    state: State<ScannerState>,
){
    val isVisible = remember { mutableStateOf(false) }
    val textOrientation = remember { mutableStateOf(OrientationTextPosition.TOP) }
    val isLandscape = state.value.cameraRotationValue == 90f || state.value.cameraRotationValue == 270f
    LaunchedEffect(state.value.cameraRotationValue){
        textOrientation.value = when(state.value.cameraRotationValue){
            0f -> OrientationTextPosition.TOP
            270f -> OrientationTextPosition.RIGHT
            180f -> OrientationTextPosition.BOTTOM
            90f -> OrientationTextPosition.LEFT
            else -> OrientationTextPosition.TOP
        }
        isVisible.value = true
        delay(1000)
        isVisible.value = false
    }
    val fadeInTween = tween<Float>(
        durationMillis = 500,
        delayMillis = 200
    )
    AnimatedVisibility(
        visible = isVisible.value,
        modifier = Modifier
            .align(textOrientation.value.toAlignment())
            .rotate(
                when (textOrientation.value) {
                    OrientationTextPosition.TOP -> 0f
                    OrientationTextPosition.LEFT -> 270f
                    OrientationTextPosition.BOTTOM -> 180f
                    OrientationTextPosition.RIGHT -> 90f
                }
            )
            .offset(
                0.dp,
                when (textOrientation.value) {
                    OrientationTextPosition.LEFT, OrientationTextPosition.RIGHT -> -(50).dp
                    else -> 0.dp
                }
            ),
        enter = fadeIn(fadeInTween) + scaleIn(),
        exit = fadeOut(fadeInTween) + scaleOut()
    ) {
        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium),
        ){
            Text(
                text = if (!isLandscape) "Portrait mode" else "Landscape Mode",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}


@Composable
fun ScannerAlertSnackbar(
    snackbarHostState: SnackbarHostState,
    snackbarState: SnackbarState,
    onDismiss: () -> Unit = {},
) {
    LaunchedEffect(snackbarState) {
        when (snackbarState) {
            is SnackbarState.ShowSuccess -> {
                snackbarHostState.showSuccessSnackbar(
                    message = snackbarState.title,
                    onDismiss = onDismiss
                )
            }

            is SnackbarState.ShowWarning -> {
                snackbarHostState.showWarningSnackbar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = onDismiss
                )
            }

            is SnackbarState.ShowError -> {
                snackbarHostState.showErrorSnackBar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ScannerViewDialog(
    state: State<ScannerState>,
    cachedFilterBitmaps: List<Bitmap>?,
    onCacheBitmap: (bitmaps: List<Bitmap>) -> Unit,
    dismissAlert: () -> Unit,
    deleteDocument: () -> Unit,
    applyFilterProcess: suspend (bitmap: Bitmap) -> List<Bitmap>,
    applyFilter: (filter: ImageFilter) -> Unit,
    onNavigateBack: () -> Unit
) {
    if (state.value.dialogState == null) return
    when (state.value.dialogState!!) {
        is ScannerDialogState.DeleteImage -> {
            ScannerDialog(
                title = "Delete Image",
                message = "Are you sure you want to delete this image?",
                positiveButtonText = "Delete",
                negativeButtonText = "Cancel",
                onDismiss = { dismissAlert() },
                onConfirm = { deleteDocument() },
                icon = Icons.Default.DeleteForever
            )
        }
        is ScannerDialogState.ExitScanner -> {
            ScannerDialog(
                title = "Exit Scanner",
                message = "You are about to exit the scanner, all scanned images will be lost\n\n Are you sure you want to exit?",
                positiveButtonText = "Exit",
                negativeButtonText = "Cancel",
                onDismiss = { dismissAlert() },
                onConfirm = onNavigateBack,
                icon = Icons.AutoMirrored.Rounded.ExitToApp
            )
        }
        is ScannerDialogState.FilterImage -> {
            ShowFilterDialog(
                onDismiss = { dismissAlert() },
                cachedBitmaps = cachedFilterBitmaps,
                onCacheBitmap = onCacheBitmap,
                scannedDocument = state.value.scannedDocuments[state.value.currentDocumentIndex],
                applyFilterFunction = { bitmap -> applyFilterProcess(bitmap) },
                onFilterSelected = { filter -> applyFilter(filter) },
            )
        }
        is ScannerDialogState.UnsavedDocument -> {
            val dialogState = state.value.dialogState as ScannerDialogState.UnsavedDocument
            ScannerDialog(
                title = "Change to ${dialogState.nextTab.toFormattedString()}",
                message = "You have unsaved changes, are you sure you want to change the tab?",
                onDismiss = { dismissAlert() },
                onConfirm = {
                    dialogState.onChangeTab.invoke()
                    dismissAlert()
                },
                icon = Icons.Rounded.WarningAmber
            )
        }
        is ScannerDialogState.SaveDocument -> {
            SavingDocument(
                state = state,
                onDismiss = { dismissAlert() }
            )
        }
    }
}

@Composable
fun ScannerDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconSize: Dp = 120.dp,
    positiveButtonText: String = "Ok",
    negativeButtonText: String = "Cancel",
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
){
    IconAlertDialog(
        onDismiss = onDismiss,
        icon = icon,
        size = iconSize,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        onCancel = onDismiss,
        cancelText = negativeButtonText,
        onConfirm = onConfirm,
        confirmText = positiveButtonText
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingDocument(
    state: State<ScannerState>,
    onDismiss: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ){
        Surface(
            shape = RoundedCornerShape(24.dp),
        ){
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Saving document...",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowBarCodeBottomSheet(
    barCode: Barcode?,
    onDismiss: () -> Unit,
){
    val hapticFeedback = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    LaunchedEffect(barCode){
        if (barCode != null && barCode.rawValue.isNullOrBlank().not()){
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            sheetState.show()
        }
    }
    if (barCode != null && barCode.rawValue.isNullOrBlank().not()) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
                if (barCode.sms != null) {
                    ShowBarcodeInfo(
                        icon = Icons.AutoMirrored.Filled.Message,
                        title = "SMS Info",
                        messageValue = "Phone: ${barCode.sms?.phoneNumber}\nMessage: ${barCode.sms?.message}",
                        message = {
                            if (barCode.sms?.phoneNumber != null) {
                                Text(
                                    text = "Phone: ${barCode.sms?.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text(
                                text = "Message: ${if (barCode.sms?.message.isNullOrEmpty()) "-----" else barCode.sms?.message}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    )

                } else if (barCode.url != null) {
                    ShowBarcodeInfo(
                        icon = Icons.AutoMirrored.Filled.Launch,
                        title = "URL",
                        messageValue = barCode.url?.url ?: "",
                        message = {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(barCode.url?.url)
                                        )
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    barCode.url?.url ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = TextDecoration.Underline,
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                    )
                } else if (barCode.email != null) {
                    ShowBarcodeInfo(
                        icon = Icons.AutoMirrored.Filled.Message,
                        title = "Email",
                        messageValue = "Email: ${barCode.email?.address} Subject: ${barCode.email?.subject} Body: ${barCode.email?.body}",
                        message = {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO)
                                        intent.data = Uri.parse(
                                            "mailto:${barCode.email?.address}?subject=${barCode.email?.subject}&body=${barCode.email?.body}"
                                        )
                                        context.startActivity(intent)
                                    }
                            ) {
                                Text(
                                    text = "${barCode.email?.address}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = TextDecoration.Underline,
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                )
                            }
                            Text(
                                text = "Subject: ${if (barCode.email?.subject.isNullOrEmpty()) "-----" else barCode.email?.subject}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Body: ${if (barCode.email?.body.isNullOrEmpty()) "-----" else barCode.email?.body}",
                                style = MaterialTheme.typography.bodyMedium,
                            )

                        },
                    )
                } else if (barCode.contactInfo != null) {
                    ShowBarcodeInfo(
                        icon = Icons.AutoMirrored.Filled.Message,
                        title = "Contact Info",
                        messageValue = "Name: ${barCode.contactInfo?.name?.formattedName}\nPhone: ${barCode.contactInfo?.phones?.joinToString { it.number.toString() }}\nEmail: ${barCode.contactInfo?.emails?.joinToString { it.address.toString() }}\nAddress: ${barCode.contactInfo?.addresses?.joinToString { it.addressLines.toString() }}\nOrganization: ${barCode.contactInfo?.organization?.toString()}",
                        message = {
                            Text(
                                text = "Name: ${barCode.contactInfo?.name?.formattedName}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (barCode.contactInfo?.phones?.isNotEmpty() == true) {
                                Text(
                                    text = "Phone: ${barCode.contactInfo?.phones?.joinToString { it.number.toString() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            if (barCode.contactInfo?.emails?.isNotEmpty() == true) {
                                Text(
                                    text = "Email: ${barCode.contactInfo?.emails?.joinToString { it.address.toString() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            if (barCode.contactInfo?.addresses?.isNotEmpty() == true) {
                                Text(
                                    text = "Address: ${barCode.contactInfo?.addresses?.joinToString { it.addressLines.toString() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            if (barCode.contactInfo?.urls?.isNotEmpty() == true) {
                                barCode.contactInfo?.urls?.forEach {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable {
                                                val intent =
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                                context.startActivity(intent)
                                            }
                                    )
                                }
                            }
                            if (barCode.contactInfo?.organization?.isNotEmpty() == true) {
                                Text(
                                    text = "Organization: ${barCode.contactInfo?.organization?.toString()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    )
                } else if (barCode.phone != null) {
                    ShowBarcodeInfo(
                        icon = Icons.AutoMirrored.Filled.Message,
                        title = "Phone",
                        messageValue = barCode.phone?.number ?: "",
                        message = {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:${barCode.phone?.number}")
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = barCode.phone?.number ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                } else if (barCode.wifi != null) {
                    val clipboardManager: ClipboardManager = LocalClipboardManager.current
                    ShowBarcodeInfo(
                        icon = Icons.Rounded.QrCodeScanner,
                        title = "Wifi",
                        messageValue = "SSID: ${barCode.wifi?.ssid}\nPassword: ${barCode.wifi?.password}",
                        message = {
                            Column(
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(
                                            AnnotatedString(
                                                barCode.wifi?.password ?: ""
                                            )
                                        )
                                        val intent =
                                            Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                                        context.startActivity(intent)
                                        Toast.makeText(
                                            context,
                                            "Look for ${barCode.wifi?.ssid ?: ""}. Password is copied ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Row {
                                    Text(
                                        text = barCode.wifi?.ssid ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Wifi,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Password: ${
                                        barCode.wifi?.password?.replaceAfter(
                                            5,
                                            "*"
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    )
                } else if (barCode.displayValue != null) {
                    ShowBarcodeInfo(
                        icon = Icons.Rounded.QrCodeScanner,
                        title = "Barcode",
                        messageValue = barCode.displayValue ?: "",
                        message = {
                            Text(
                                text = barCode.displayValue ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun String?.replaceAfter(i: Int, s: String): String {
    return if (this != null && this.length > i){
        this.substring(0, i) + (s.repeat(this.length - i))
    }else{
        this ?: ""
    }
}

@Composable
fun ShowBarcodeInfo(
    icon: ImageVector,
    title: String,
    messageValue: String,
    message: @Composable () -> Unit,
){
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = "$title :",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.size(4.dp))
            message()
        }
        Box(
            modifier = Modifier
                .clickable {
                    clipboardManager.setText(AnnotatedString(messageValue))
                }
                .gradientContainer(
                    RoundedCornerShape(
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Copy",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun Context.openAppSettings() {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addCategory(CATEGORY_DEFAULT)
        addFlags(FLAG_ACTIVITY_NEW_TASK)
        addFlags(FLAG_ACTIVITY_NO_HISTORY)
        addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    startActivity(intent)
}



