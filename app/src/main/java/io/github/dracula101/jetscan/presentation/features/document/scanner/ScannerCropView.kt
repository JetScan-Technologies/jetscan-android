package io.github.dracula101.jetscan.presentation.features.document.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.magnifier
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.data.platform.utils.aspectRatio
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.detectTransformGestures
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.CornerPointVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.HolderVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toCropOverlay
import timber.log.Timber

@Composable
fun ScannerCropView(
    state: ScannerState,
    scannedDocument: CameraScannedImage,
    viewModel: ScannerViewModel,
) {
    val imageCropCoords = remember { mutableStateOf<ImageCropCoords?>(null) }
    val currentPositionCoords = remember { mutableStateOf<CPoint?>(null) }
    val boundary = remember { mutableStateOf<Size?>(null) }
    val currentPointPosition = remember { mutableStateOf<PointPosition?>(null) }
    Timber.i(
        "Documents: ${state.scannedDocuments}, Current: ${state.scannedDocuments.indexOf(scannedDocument)}"
    )
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    horizontal = 8.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .aspectRatio(3/4f)
                    .clipToBounds()
            ) {
                // Image
                AsyncImage(
                    model = scannedDocument.originalImage,
                    contentDescription = "Document Image",
                    modifier = Modifier
                        .align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(scannedDocument.originalImage.aspectRatio())
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ){

                    BoxWithConstraints {
                        boundary.value = Size(
                            constraints.maxWidth.toFloat(),
                            constraints.maxHeight.toFloat()
                        )
                        val imageSpaceSize = Size(
                            constraints.maxWidth.toFloat(),
                            constraints.maxHeight.toFloat()
                        )
                        val originalImageSize = Size(
                            scannedDocument.originalImage.width.toFloat(),
                            scannedDocument.originalImage.height.toFloat()
                        )
                        val widthRatio = imageSpaceSize.width / originalImageSize.width
                        val heightRatio = imageSpaceSize.height / originalImageSize.height
                        Timber.i("Image coords: ${scannedDocument.cropCoords}")
                        Timber.i("Width Ratio: $widthRatio, Height Ratio: $heightRatio, Image Space Size: $imageSpaceSize, Original Image Size: $originalImageSize, Boundary: ${boundary.value}")
                        if (imageCropCoords.value == null) {
                            imageCropCoords.value = scannedDocument.cropCoords.scale(
                                scaleX = widthRatio,
                                scaleY = heightRatio
                            ).bound(
                                boundary = boundary.value!!
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .zIndex(3f)
                                .aspectRatio(
                                    scannedDocument.originalImage.aspectRatio()
                                )
                        ) {
                            imageCropCoords.value?.let { cropCoords ->
                                toCropOverlay(
                                    imageCropCoords = cropCoords,
                                    cornerPointVisibility = CornerPointVisibility(
                                        topLeft = PointPosition.TOP_LEFT != currentPointPosition.value,
                                        topRight = PointPosition.TOP_RIGHT != currentPointPosition.value,
                                        bottomLeft = PointPosition.BOTTOM_LEFT != currentPointPosition.value,
                                        bottomRight = PointPosition.BOTTOM_RIGHT != currentPointPosition.value
                                    ),
                                    holderVisibility = HolderVisibility(
                                        topCenter = PointPosition.TOP_CENTER != currentPointPosition.value,
                                        rightCenter = PointPosition.RIGHT_CENTER != currentPointPosition.value,
                                        bottomCenter = PointPosition.BOTTOM_CENTER != currentPointPosition.value,
                                        leftCenter = PointPosition.LEFT_CENTER != currentPointPosition.value
                                    )
                                )
                            }
                        }
                        // Draw Magnifier
                        if (currentPositionCoords.value != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        scannedDocument.originalImage.aspectRatio()
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .zIndex(0f)
                                        .magnifier(
                                            sourceCenter = {
                                                currentPositionCoords.value?.let {
                                                    Offset(it.x.toFloat(), it.y.toFloat())
                                                } ?: Offset.Zero
                                            },
                                            size = DpSize(100.dp, 100.dp),
                                            magnifierCenter = {
                                                Offset(
                                                    when (currentPointPosition.value) {
                                                        PointPosition.TOP_LEFT -> boundary.value!!.width - 100f
                                                        else -> 0f
                                                    },
                                                    50f
                                                )
                                            },
                                            cornerRadius = 50.dp,
                                            zoom = 2f,
                                        )
                                )
                            }
                        }

                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                scannedDocument.originalImage.aspectRatio()
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures(
                                    onGestureStart = {
                                        val newImageCropCoords = calculateNewImageCropCoords(
                                            imageCropCoords = imageCropCoords.value
                                                ?: ImageCropCoords.NONE,
                                            currentLockedPoint = currentPointPosition.value,
                                            inputChange = it,
                                            onPositionChange = {
                                                currentPointPosition.value = it
                                            },
                                            boundary = boundary.value
                                        )
                                        imageCropCoords.value = newImageCropCoords
                                        currentPositionCoords.value = CPoint(
                                            it.position.x.toDouble(),
                                            it.position.y.toDouble()
                                        )
                                    },
                                    onGesture = { _, _, _, _, _, pointerList ->
                                        for (mainPointerInputChange in pointerList) {
                                            val newImageCropCoords =
                                                calculateNewImageCropCoords(
                                                    imageCropCoords = imageCropCoords.value
                                                        ?: ImageCropCoords.NONE,
                                                    currentLockedPoint = currentPointPosition.value,
                                                    inputChange = mainPointerInputChange,
                                                    onPositionChange = {
                                                        currentPointPosition.value = it
                                                    },
                                                    boundary = boundary.value
                                                )
                                            imageCropCoords.value = newImageCropCoords
                                            currentPositionCoords.value = CPoint(
                                                mainPointerInputChange.position.x.toDouble(),
                                                mainPointerInputChange.position.y.toDouble()
                                            )
                                        }
                                    },
                                    onGestureEnd = {
                                        val newImageCropCoords = calculateNewImageCropCoords(
                                            imageCropCoords = imageCropCoords.value
                                                ?: ImageCropCoords.NONE,
                                            currentLockedPoint = currentPointPosition.value,
                                            inputChange = it,
                                            onPositionChange = {
                                                currentPointPosition.value = null
                                            },
                                            boundary = boundary.value
                                        )
                                        imageCropCoords.value = newImageCropCoords
                                        currentPositionCoords.value = null
                                    }
                                )
                            }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Crop Document",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Drag the corners to crop the document",
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 24.dp
                    )
                )
                Text(
                    "Change Document Name",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.documentName,
                    onValueChange = {
                        viewModel.trySendAction(ScannerAction.EditAction.DocumentChangeName(it))
                    },
                    label = {
                        Text("Document Name")
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if(state.retakeDocumentIndex==null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.trySendAction(
                                    ScannerAction.EditAction.RetakeDocument(
                                        state.scannedDocuments.size - 1
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text("Retake")
                        }
                    }
                    FilledTonalButton(
                        modifier = Modifier
                            .weight(2f),
                        onClick = {
                            if (imageCropCoords.value != null) {
                                val imageSize = Size(
                                    scannedDocument.originalImage.width.toFloat(),
                                    scannedDocument.originalImage.height.toFloat()
                                )
                                val widthRatio = imageSize.width / boundary.value!!.width
                                val heightRatio = imageSize.height / boundary.value!!.height
                                viewModel.trySendAction(
                                    ScannerAction.Ui.FirstCropDocument(
                                        scannedDocument = scannedDocument.copy(
                                            cropCoords = imageCropCoords.value!!.scale(
                                                scaleX = widthRatio,
                                                scaleY = heightRatio
                                            ),
                                        )
                                    )
                                )
                            }
                        },
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

enum class PointPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP_CENTER,
    LEFT_CENTER,
    RIGHT_CENTER,
    BOTTOM_CENTER,
}


fun calculateNewImageCropCoords(
    imageCropCoords: ImageCropCoords,
    inputChange: PointerInputChange,
    currentLockedPoint: PointPosition? = null,
    onPositionChange: (PointPosition) -> Unit,
    minimumTouchDistance: Float = 130f,
    boundary: Size? = null
): ImageCropCoords {
    // i want to find the distance between the input change and the corners and then check if the minimum distance is less than the minimum touch distance
    val topLeftDistance = imageCropCoords.topLeft.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val topRightDistance = imageCropCoords.topRight.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val bottomLeftDistance = imageCropCoords.bottomLeft.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val bottomRightDistance = imageCropCoords.bottomRight.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    // distance for the center
    val topCenter = CPoint(
        x = (imageCropCoords.topLeft.x + imageCropCoords.topRight.x) / 2,
        y = (imageCropCoords.topLeft.y + imageCropCoords.topRight.y) / 2
    )
    val topCenterDistance = topCenter.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val leftCenter = CPoint(
        x = (imageCropCoords.topLeft.x + imageCropCoords.bottomLeft.x) / 2,
        y = (imageCropCoords.topLeft.y + imageCropCoords.bottomLeft.y) / 2
    )
    val leftCenterDistance = leftCenter.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val rightCenter = CPoint(
        x = (imageCropCoords.topRight.x + imageCropCoords.bottomRight.x) / 2,
        y = (imageCropCoords.topRight.y + imageCropCoords.bottomRight.y) / 2
    )
    val rightCenterDistance = rightCenter.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )
    val bottomCenter = CPoint(
        x = (imageCropCoords.bottomLeft.x + imageCropCoords.bottomRight.x) / 2,
        y = (imageCropCoords.bottomLeft.y + imageCropCoords.bottomRight.y) / 2
    )
    val bottomCenterDistance = bottomCenter.distanceTo(
        Offset(x = inputChange.position.x,y = inputChange.position.y)
    )

    val minDistance = listOf(
        topLeftDistance,
        topRightDistance,
        bottomLeftDistance,
        bottomRightDistance
    ).minOrNull() ?: 0f
    val centerDistance = listOf(
        topCenterDistance,
        leftCenterDistance,
        rightCenterDistance,
        bottomCenterDistance
    ).minOrNull() ?: 0f

    if (minDistance < minimumTouchDistance) {
        val changePoint = when (minDistance) {
            topLeftDistance -> CPoint(imageCropCoords.topLeft.x, imageCropCoords.topLeft.y)
            topRightDistance -> CPoint(imageCropCoords.topRight.x, imageCropCoords.topRight.y)
            bottomLeftDistance -> CPoint(imageCropCoords.bottomLeft.x, imageCropCoords.bottomLeft.y)
            bottomRightDistance -> CPoint(imageCropCoords.bottomRight.x, imageCropCoords.bottomRight.y)
            else -> CPoint(0.0, 0.0)
        }
        val pointPosition = when (changePoint) {
            imageCropCoords.topLeft -> PointPosition.TOP_LEFT
            imageCropCoords.topRight -> PointPosition.TOP_RIGHT
            imageCropCoords.bottomLeft -> PointPosition.BOTTOM_LEFT
            imageCropCoords.bottomRight -> PointPosition.BOTTOM_RIGHT
            else -> PointPosition.TOP_LEFT
        }
        if (currentLockedPoint != null) {
            if (currentLockedPoint != pointPosition) {
                return imageCropCoords
            }
        }
        onPositionChange(pointPosition)
        val changedPoint = CPoint(
            x = inputChange.position.x.toDouble(),
            y = inputChange.position.y.toDouble()
        )
        val newImageCropCoords = when (changePoint) {
            imageCropCoords.topLeft -> imageCropCoords.copy(topLeft = changedPoint)
            imageCropCoords.topRight -> imageCropCoords.copy(topRight = changedPoint)
            imageCropCoords.bottomLeft -> imageCropCoords.copy(bottomLeft = changedPoint)
            imageCropCoords.bottomRight -> imageCropCoords.copy(bottomRight = changedPoint)
            else -> imageCropCoords
        }
        return newImageCropCoords.bound(
            boundary = boundary ?: Size(0f, 0f)
        )
    }
    if (centerDistance < minimumTouchDistance) {
        val changePoint = when (centerDistance) {
            topCenterDistance -> PointPosition.TOP_CENTER
            leftCenterDistance -> PointPosition.LEFT_CENTER
            rightCenterDistance -> PointPosition.RIGHT_CENTER
            bottomCenterDistance -> PointPosition.BOTTOM_CENTER
            else -> PointPosition.TOP_CENTER
        }
        if (currentLockedPoint != null) {
            if (currentLockedPoint != changePoint) {
                return imageCropCoords
            }
        }
        onPositionChange(changePoint)
        val absoluteDistance = Offset(
            x = inputChange.position.x - inputChange.previousPosition.x,
            y = inputChange.position.y - inputChange.previousPosition.y
        )
        val newImageCropCoords = when (changePoint) {
            PointPosition.TOP_CENTER -> imageCropCoords.copy(
                topLeft = CPoint(
                    x = imageCropCoords.topLeft.x + absoluteDistance.x,
                    y = imageCropCoords.topLeft.y + absoluteDistance.y
                ),
                topRight = CPoint(
                    x = imageCropCoords.topRight.x + absoluteDistance.x,
                    y = imageCropCoords.topRight.y + absoluteDistance.y
                )
            )
            PointPosition.LEFT_CENTER -> imageCropCoords.copy(
                topLeft = CPoint(
                    x = imageCropCoords.topLeft.x + absoluteDistance.x,
                    y = imageCropCoords.topLeft.y + absoluteDistance.y
                ),
                bottomLeft = CPoint(
                    x = imageCropCoords.bottomLeft.x + absoluteDistance.x,
                    y = imageCropCoords.bottomLeft.y + absoluteDistance.y
                )
            )
            PointPosition.RIGHT_CENTER -> imageCropCoords.copy(
                topRight = CPoint(
                    x = imageCropCoords.topRight.x + absoluteDistance.x,
                    y = imageCropCoords.topRight.y + absoluteDistance.y
                ),
                bottomRight = CPoint(
                    x = imageCropCoords.bottomRight.x + absoluteDistance.x,
                    y = imageCropCoords.bottomRight.y + absoluteDistance.y
                )
            )
            PointPosition.BOTTOM_CENTER -> imageCropCoords.copy(
                bottomLeft = CPoint(
                    x = imageCropCoords.bottomLeft.x + absoluteDistance.x,
                    y = imageCropCoords.bottomLeft.y + absoluteDistance.y
                ),
                bottomRight = CPoint(
                    x = imageCropCoords.bottomRight.x + absoluteDistance.x,
                    y = imageCropCoords.bottomRight.y + absoluteDistance.y
                )
            )
            else -> imageCropCoords
        }
        return newImageCropCoords.bound(
            boundary = boundary ?: Size(0f, 0f)
        )
    }
    return imageCropCoords
}
