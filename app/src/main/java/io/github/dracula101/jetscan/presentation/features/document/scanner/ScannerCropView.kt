package io.github.dracula101.jetscan.presentation.features.document.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.data.platform.utils.aspectRatio
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.detectTransformGestures
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toPath
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords

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
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3 / 4f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = scannedDocument.originalImage,
                        contentDescription = "Document Image",
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3 / 4f),
                    contentAlignment = Alignment.Center
                ){
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                scannedDocument.originalImage.aspectRatio()
                            )
                    ) {
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
                        if (imageCropCoords.value == null) {
                            imageCropCoords.value = scannedDocument.cropCoords.scale(
                                scaleX = widthRatio,
                                scaleY = heightRatio
                            )
                        }
                        val cropRect = imageCropCoords.value?.toPath()
                        val cropRectStroke = Stroke(4.dp.value)
                        val radius = 30.dp.value
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            if (cropRect != null) {
                                drawPath(
                                    path = cropRect,
                                    color = Color.White,
                                    style = cropRectStroke,
                                )
                            }
                            // Draw corners
                            if (imageCropCoords.value != null) {
                                if (currentPointPosition.value != PointPosition.TOP_LEFT)
                                    drawCircle(
                                        color = Color.White,
                                        radius = radius,
                                        style = cropRectStroke,
                                        center = imageCropCoords.value!!.topLeft.toOffset()
                                    )
                                if (currentPointPosition.value != PointPosition.TOP_RIGHT)
                                    drawCircle(
                                        color = Color.White,
                                        radius = radius,
                                        style = cropRectStroke,
                                        center = imageCropCoords.value!!.topRight.toOffset()
                                    )
                                if (currentPointPosition.value != PointPosition.BOTTOM_LEFT)
                                    drawCircle(
                                        color = Color.White,
                                        radius = radius,
                                        style = cropRectStroke,
                                        center = imageCropCoords.value!!.bottomLeft.toOffset()
                                    )
                                if (currentPointPosition.value != PointPosition.BOTTOM_RIGHT)
                                    drawCircle(
                                        color = Color.White,
                                        radius = radius,
                                        style = cropRectStroke,
                                        center = imageCropCoords.value!!.bottomRight.toOffset()
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
                        // Draw overlay
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
                        .fillMaxWidth(0.9f)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.trySendAction(ScannerAction.EditAction.RetakeDocument(state.scannedDocuments.size - 1))
                        },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text("Retake")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
    BOTTOM_RIGHT
}


fun calculateNewImageCropCoords(
    imageCropCoords: ImageCropCoords,
    inputChange: PointerInputChange,
    onPositionChange: (PointPosition) -> Unit,
    boundary: Size? = null
): ImageCropCoords {
    val inputPosition = Offset(inputChange.position.x, inputChange.position.y)
    val topLeftDistance = imageCropCoords.topLeft.distanceTo(inputPosition)
    val topRightDistance = imageCropCoords.topRight.distanceTo(inputPosition)
    val bottomLeftDistance = imageCropCoords.bottomLeft.distanceTo(inputPosition)
    val bottomRightDistance = imageCropCoords.bottomRight.distanceTo(inputPosition)
    val minDistance = listOf(
        topLeftDistance,
        topRightDistance,
        bottomLeftDistance,
        bottomRightDistance
    ).minOrNull() ?: 0f
    val changePoint = when (minDistance) {
        topLeftDistance -> CPoint(imageCropCoords.topLeft.x, imageCropCoords.topLeft.y)
        topRightDistance -> CPoint(imageCropCoords.topRight.x, imageCropCoords.topRight.y)
        bottomLeftDistance -> CPoint(imageCropCoords.bottomLeft.x, imageCropCoords.bottomLeft.y)
        bottomRightDistance -> CPoint(imageCropCoords.bottomRight.x, imageCropCoords.bottomRight.y)
        else -> CPoint(0.0, 0.0)
    }
    val changedPoint = CPoint(
        x = inputChange.position.x.toDouble(),
        y = inputChange.position.y.toDouble()
    )
    onPositionChange(
        when (changePoint) {
            imageCropCoords.topLeft -> PointPosition.TOP_LEFT
            imageCropCoords.topRight -> PointPosition.TOP_RIGHT
            imageCropCoords.bottomLeft -> PointPosition.BOTTOM_LEFT
            imageCropCoords.bottomRight -> PointPosition.BOTTOM_RIGHT
            else -> PointPosition.TOP_LEFT
        }
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
