package io.github.dracula101.jetscan.presentation.features.document.scanner

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.data.platform.utils.aspectRatio
import io.github.dracula101.jetscan.presentation.platform.component.button.SegmentedButton
import io.github.dracula101.jetscan.presentation.platform.component.button.SegmentedItem
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.detectTransformGestures
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.CornerPointVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.HolderVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toCropOverlay
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ScannerCropView(
    state: ScannerState,
    scannedDocument: CameraScannedImage,
    viewModel: ScannerViewModel,
) {
    val isLoadingFilter = remember { mutableStateOf(false) }
    val isPreviewCropLoading = remember { mutableStateOf(false) }

    val imageCropCoords = remember { mutableStateOf<ImageCropCoords?>(null) }
    val currentPositionCoords = remember { mutableStateOf<CPoint?>(null) }
    val filterBitmaps = remember { mutableStateOf<List<Bitmap>?>(null) }
    val boundary = remember { mutableStateOf<Size?>(null) }
    val currentPointPosition = remember { mutableStateOf<PointPosition?>(null) }
    val rotationAnim = remember { Animatable(0f) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editScreenBottomSheet = remember { mutableStateOf<CropEditBottomSheet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val segmentedItems = remember { mutableStateOf(DocumentTab.ORIGINAL) }
    val horizontalPagerState = rememberPagerState { 2 }
    if (editScreenBottomSheet.value != null) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                    editScreenBottomSheet.value = null
                }
            }
        ) {
            when (editScreenBottomSheet.value) {
                CropEditBottomSheet.DOC_NAME -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Document Name",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.documentName,
                            onValueChange = {
                                viewModel.trySendAction(
                                    ScannerAction.EditAction.DocumentChangeName(it)
                                )
                            },
                            label = {
                                Text("Document Name")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editScreenBottomSheet.value = null
                                    coroutineScope.launch {
                                        bottomSheetState.hide()
                                    }
                                }
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }

                CropEditBottomSheet.FILTER -> {
                    if (isLoadingFilter.value || filterBitmaps.value != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(0.8f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Apply Filter",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Filtered Image can be seen in the Cropped Tab",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(filterBitmaps.value?.size ?: 0) { index ->
                                    Box(
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.trySendAction(
                                                    ScannerAction.EditAction.ApplyFilter(
                                                        filter = ImageFilter.entries[index],
                                                        index = state.scannedDocuments.size - 1
                                                    )
                                                )
                                                editScreenBottomSheet.value = null
                                                coroutineScope.launch {
                                                    bottomSheetState.hide()
                                                }
                                            }
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = filterBitmaps.value!![index],
                                                contentDescription = "Filter Image",
                                                modifier = Modifier
                                                    .clip(MaterialTheme.shapes.small)
                                                    .aspectRatio(scannedDocument.originalImage.aspectRatio())
                                                    .width(80.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                ImageFilter.entries[index].toFormattedString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (scannedDocument.imageEffect.imageFilter == ImageFilter.entries[index]) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                ),
                                                color = if (scannedDocument.imageEffect.imageFilter == ImageFilter.entries[index]) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Apply Filter",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Loading Filters...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                null -> {

                }
            }
        }
    }

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
            // Scanned / preview (segmented tabs)
            SegmentedButton(
                items = listOf(
                    SegmentedItem(
                        title = DocumentTab.ORIGINAL.toFormattedString(),
                        onClick = {
                            segmentedItems.value = DocumentTab.ORIGINAL
                            coroutineScope.launch {
                                horizontalPagerState.animateScrollToPage(
                                    0,
                                    animationSpec = tween(
                                        durationMillis = 800
                                    )
                                )
                            }
                        }
                    ),
                    SegmentedItem(
                        title = DocumentTab.CROPPED.toFormattedString(),
                        onClick = {
                            segmentedItems.value = DocumentTab.CROPPED
                            coroutineScope.launch {
                                horizontalPagerState.animateScrollToPage(
                                    1,
                                    animationSpec = tween(
                                        durationMillis = 800
                                    )
                                )
                            }
                            isPreviewCropLoading.value = true
                            val imageSize = Size(
                                scannedDocument.originalImage.width.toFloat(),
                                scannedDocument.originalImage.height.toFloat()
                            )
                            val widthRatio = imageSize.width / boundary.value!!.width
                            val heightRatio = imageSize.height / boundary.value!!.height
                            viewModel.trySendAction(
                                ScannerAction.Ui.PreviewCropDocument(
                                    cropCoords = imageCropCoords.value!!.scale(
                                        scaleX = widthRatio,
                                        scaleY = heightRatio
                                    ),
                                    onCompleteCallback = {
                                        isPreviewCropLoading.value = false
                                    }
                                )
                            )
                        }
                    )
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 4f),
                userScrollEnabled = false,
                pageContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Box(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            contentAlignment = Alignment.Center
                        ){
                            when (it) {
                                0 -> {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .aspectRatio(3 / 4f)
                                            .clipToBounds(),
                                    ) {
                                        // Image
                                        AsyncImage(
                                            model = scannedDocument.originalImage,
                                            contentDescription = "Document Image",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .rotate(rotationAnim.value)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .aspectRatio(
                                                    scannedDocument.originalImage.aspectRatio()
                                                )
                                                .clipToBounds()
                                                .rotate(rotationAnim.value),
                                            contentAlignment = Alignment.Center
                                        ) {

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
                                                val widthRatio =
                                                    imageSpaceSize.width / originalImageSize.width
                                                val heightRatio =
                                                    imageSpaceSize.height / originalImageSize.height
                                                if (imageCropCoords.value == null) {
                                                    imageCropCoords.value =
                                                        scannedDocument.cropCoords.scale(
                                                            scaleX = widthRatio,
                                                            scaleY = heightRatio
                                                        ).bound(
                                                            boundary = boundary.value!!
                                                        )
                                                }
                                                Canvas(
                                                    modifier = Modifier
                                                        .aspectRatio(scannedDocument.originalImage.aspectRatio())
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
                                                            .aspectRatio(scannedDocument.originalImage.aspectRatio())
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .zIndex(0f)
                                                                .magnifier(
                                                                    sourceCenter = {
                                                                        currentPositionCoords.value?.let {
                                                                            Offset(
                                                                                it.x.toFloat(),
                                                                                it.y.toFloat()
                                                                            )
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
                                                    .aspectRatio(scannedDocument.originalImage.aspectRatio())
                                                    .pointerInput(Unit) {
                                                        detectTransformGestures(
                                                            onGestureStart = {
                                                                val newImageCropCoords =
                                                                    calculateNewImageCropCoords(
                                                                        imageCropCoords = imageCropCoords.value
                                                                            ?: ImageCropCoords.NONE,
                                                                        currentLockedPoint = currentPointPosition.value,
                                                                        inputChange = it,
                                                                        onPositionChange = {
                                                                            currentPointPosition.value =
                                                                                it
                                                                        },
                                                                        boundary = boundary.value
                                                                    )
                                                                imageCropCoords.value =
                                                                    newImageCropCoords
                                                                currentPositionCoords.value =
                                                                    CPoint(
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
                                                                            onPositionChange = {},
                                                                            boundary = boundary.value
                                                                        )
                                                                    imageCropCoords.value =
                                                                        newImageCropCoords
                                                                    currentPositionCoords.value =
                                                                        CPoint(
                                                                            mainPointerInputChange.position.x.toDouble(),
                                                                            mainPointerInputChange.position.y.toDouble()
                                                                        )
                                                                }
                                                            },
                                                            onGestureEnd = {
                                                                val newImageCropCoords =
                                                                    calculateNewImageCropCoords(
                                                                        imageCropCoords = imageCropCoords.value
                                                                            ?: ImageCropCoords.NONE,
                                                                        currentLockedPoint = currentPointPosition.value,
                                                                        inputChange = it,
                                                                        onPositionChange = {
                                                                            currentPointPosition.value =
                                                                                null
                                                                        },
                                                                        boundary = boundary.value
                                                                    )
                                                                imageCropCoords.value =
                                                                    newImageCropCoords
                                                                currentPositionCoords.value = null
                                                            }
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    if (isPreviewCropLoading.value) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Loading Cropped Image...",
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = scannedDocument.filteredImage
                                                ?: scannedDocument.croppedImage,
                                            contentDescription = "Document Image",
                                            modifier = Modifier
                                                .rotate(rotationAnim.value)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
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
                        bottom = 12.dp
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ColumnItem(
                        title = "Doc Name",
                        icon = Icons.Rounded.Description,
                        onClick = {
                            editScreenBottomSheet.value = CropEditBottomSheet.DOC_NAME
                            coroutineScope.launch {
                                bottomSheetState.show()
                            }
                        }
                    )
                    ColumnItem(
                        title = "Rotate",
                        icon = Icons.AutoMirrored.Rounded.RotateRight,
                        onClick = {
                            val currentRotation = rotationAnim.value
                            coroutineScope.launch {
                                rotationAnim.animateTo(
                                    targetValue = (currentRotation + 90f) % 360,
                                )
                            }
                        }
                    )
                    ColumnItem(
                        title = "Filter",
                        icon = Icons.Rounded.Filter,
                        onClick = {
                            editScreenBottomSheet.value = CropEditBottomSheet.FILTER
                            coroutineScope.launch {
                                bottomSheetState.show()
                                filterBitmaps.value =
                                    viewModel.applyFilters(index = state.scannedDocuments.size - 1)
                                bottomSheetState.expand()
                                isLoadingFilter.value = true
                            }
                        }
                    )
                }
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
                    if (state.retakeDocumentIndex == null) {
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
                                    ScannerAction.EditAction.RotateDocument(
                                        index = state.scannedDocuments.size - 1,
                                        rotation = rotationAnim.value.toInt()
                                    )
                                )
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

@Composable
fun RowScope.ColumnItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
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
    minimumTouchDistance: Float = 150f,
    boundary: Size? = null
): ImageCropCoords {
    // i want to find the distance between the input change and the corners and then check if the minimum distance is less than the minimum touch distance
    val topLeftDistance = imageCropCoords.topLeft.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val topRightDistance = imageCropCoords.topRight.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val bottomLeftDistance = imageCropCoords.bottomLeft.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val bottomRightDistance = imageCropCoords.bottomRight.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    // distance for the center
    val topCenter = CPoint(
        x = (imageCropCoords.topLeft.x + imageCropCoords.topRight.x) / 2,
        y = (imageCropCoords.topLeft.y + imageCropCoords.topRight.y) / 2
    )
    val topCenterDistance = topCenter.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val leftCenter = CPoint(
        x = (imageCropCoords.topLeft.x + imageCropCoords.bottomLeft.x) / 2,
        y = (imageCropCoords.topLeft.y + imageCropCoords.bottomLeft.y) / 2
    )
    val leftCenterDistance = leftCenter.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val rightCenter = CPoint(
        x = (imageCropCoords.topRight.x + imageCropCoords.bottomRight.x) / 2,
        y = (imageCropCoords.topRight.y + imageCropCoords.bottomRight.y) / 2
    )
    val rightCenterDistance = rightCenter.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
    )
    val bottomCenter = CPoint(
        x = (imageCropCoords.bottomLeft.x + imageCropCoords.bottomRight.x) / 2,
        y = (imageCropCoords.bottomLeft.y + imageCropCoords.bottomRight.y) / 2
    )
    val bottomCenterDistance = bottomCenter.distanceTo(
        Offset(x = inputChange.position.x, y = inputChange.position.y)
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
            bottomRightDistance -> CPoint(
                imageCropCoords.bottomRight.x,
                imageCropCoords.bottomRight.y
            )

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

enum class CropEditBottomSheet {
    DOC_NAME,
    FILTER,
}

enum class DocumentTab {
    ORIGINAL,
    CROPPED;

    fun toFormattedString(): String {
        return when (this) {
            ORIGINAL -> "Original"
            CROPPED -> "Cropped"
        }
    }
}