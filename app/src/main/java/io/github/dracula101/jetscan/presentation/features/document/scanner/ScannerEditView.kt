package io.github.dracula101.jetscan.presentation.features.document.scanner

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FilterHdr
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoFilter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.button.clickableWithoutRipple
import io.github.dracula101.jetscan.presentation.platform.component.dialog.AppBasicDialog
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageEffect
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageFilter
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerEditView(
    backToCamera: () -> Unit,
    state: ScannerState,
    viewModel: ScannerViewModel,
    initialDocumentIndex: Int = 0,
    documents: List<CameraScannedImage>
) {
    val pagerState =
        rememberPagerState(initialPage = initialDocumentIndex, pageCount = { documents.size })
    val menuExpanded = remember { mutableStateOf(false) }
    val isEditingDocument = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val filteredImages = remember { mutableStateOf<List<Bitmap>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.trySendAction(ScannerAction.Ui.ChangeEditDocumentIndex(initialDocumentIndex))
    }
    val editOption = remember { mutableStateOf<CameraEditTab?>(null) }
    val editOptionAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                viewModel.trySendAction(ScannerAction.Ui.ChangeEditDocumentIndex(page))
                editOption.value = null
                filteredImages.value = null
            }
    }
    if (documents.isEmpty()) {
        NoDocumentFoundView(
            backToCamera = backToCamera
        )
    } else {
        Surface {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { backToCamera() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                        BasicTextField(
                            value = state.documentName,
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    isEditingDocument.value = it.isFocused
                                },
                            onValueChange = {
                                viewModel.trySendAction(
                                    ScannerAction.EditAction.DocumentChangeName(it)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textDecoration = TextDecoration.Underline
                            ),
                            singleLine = true,
                            maxLines = 1,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        )
                    }
                    Box {
                        AppDropDown(
                            expanded = menuExpanded.value,
                            offset = DpOffset((20).dp, 0.dp),
                            onDismissRequest = {
                                menuExpanded.value = false
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .shadow(32.dp),
                            items = listOf(MenuItem(title = "Edit Document", onClick = {
                                focusRequester.requestFocus()
                            }))
                        )
                        CircleButton(
                            onClick = {
                                if (!isEditingDocument.value) {
                                    menuExpanded.value = !menuExpanded.value
                                } else {
                                    focusManager.clearFocus()
                                }
                            },
                            imageVector = if (isEditingDocument.value) Icons.Rounded.Close else Icons.Rounded.MoreVert
                        )
                    }
                }
                Column(modifier = Modifier
                    .clickableWithoutRipple(
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        isEditingDocument.value = false
                        focusManager.clearFocus()
                    }
                    .then(
                        if (isEditingDocument.value) Modifier
                            .blur(8.dp)
                            .graphicsLayer(
                                alpha = 0.5f
                            )
                        else Modifier
                    )) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )
                    DocumentNavigator(pagerState, documents)
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3 / 4f)
                            .clipToBounds(),
                        state = pagerState,
                        pageSpacing = 8.dp,
                        key = { index -> documents.getOrNull(index)?.id ?: index }
                    ) { index ->
                        val document = documents.getOrNull(index)
                        if (document?.croppedImage != null) {
                            Box {
                                ScannedImageView(
                                    document.filteredImage ?: document.croppedImage,
                                    document.imageEffect,
                                )
                                ColorAdjustmentTab(
                                    if (editOption.value == CameraEditTab.COLOR_ADJUSTMENT) editOptionAnim.value else 1f,
                                    state,
                                    onContrastChange = { contrast ->
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustment(contrast = contrast)
                                        )
                                    },
                                    onBrightnessChange = { brightness ->
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustment(
                                                brightness = brightness
                                            )
                                        )
                                    },
                                    onSaturationChange = { saturation ->
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustment(
                                                saturation = saturation
                                            )
                                        )
                                    },
                                    onContrastSelected = {
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustTab(
                                                ColorAdjustTab.CONTRAST
                                            )
                                        )
                                    },
                                    onBrightnessSelected = {
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustTab(
                                                ColorAdjustTab.BRIGHTNESS
                                            )
                                        )
                                    },
                                    onSaturationSelected = {
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ChangeColorAdjustTab(
                                                ColorAdjustTab.SATURATION
                                            )
                                        )
                                    }
                                )
                                FilterTab(
                                    if (editOption.value == CameraEditTab.FILTER) editOptionAnim.value else 1f,
                                    filteredImages.value,
                                    onFilterSelected = { filter ->
                                        viewModel.trySendAction(
                                            ScannerAction.EditAction.ApplyFilter(filter)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        DocumentEditOptions(
                            viewModel = viewModel,
                            currentPage = pagerState.currentPage,
                            isColorAdjustSelected = editOption.value == CameraEditTab.COLOR_ADJUSTMENT,
                            showColorAdjustUi = {
                                coroutineScope.launch {
                                    val currentTab = editOption.value
                                    editOption.value = if (currentTab== CameraEditTab.COLOR_ADJUSTMENT) null else CameraEditTab.COLOR_ADJUSTMENT
                                    editOptionAnim.animateTo(
                                        if (currentTab == CameraEditTab.COLOR_ADJUSTMENT) 1f else 0f,
                                        animationSpec = TweenSpec(
                                            durationMillis = 500, easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            },
                            isFilterSelected = editOption.value == CameraEditTab.FILTER,
                            showFilterUi = {
                                coroutineScope.launch {
                                    val currentTab = editOption.value
                                    editOption.value = if (currentTab == CameraEditTab.FILTER) null else CameraEditTab.FILTER
                                    editOptionAnim.animateTo(
                                        if (currentTab == CameraEditTab.FILTER) 1f else 0f,
                                        animationSpec = TweenSpec(
                                            durationMillis = 500, easing = FastOutSlowInEasing
                                        )
                                    )
                                    filteredImages.value = viewModel.getCacheBitmaps(pagerState.currentPage) ?: viewModel.applyFilters()
                                }
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        )
                        DocumentEditActions(
                            onBack = backToCamera,
                            onSaveDocument = {
                                viewModel.trySendAction(ScannerAction.Ui.OnSaveDocument)
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class CameraEditTab {
    FILTER,
    COLOR_ADJUSTMENT
}

@Composable
private fun ColorAdjustmentTab(
    colorAdjustmentUiAnimOffset: Float,
    state: ScannerState,
    onContrastChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onContrastSelected: () -> Unit,
    onBrightnessSelected: () -> Unit,
    onSaturationSelected: () -> Unit,
) {
    val selectedDocument = remember { state.scannedDocuments[state.currentDocumentIndex] }
    val brightness = remember { mutableFloatStateOf(0f) }
    val contrast = remember { mutableFloatStateOf(0f) }
    val saturation = remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        brightness.floatValue = selectedDocument.imageEffect.colorAdjustment.brightness
        contrast.floatValue = selectedDocument.imageEffect.colorAdjustment.contrast
        saturation.floatValue = selectedDocument.imageEffect.colorAdjustment.saturation
        onDispose { }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .offset(y = 80.dp * colorAdjustmentUiAnimOffset)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Slider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    value = when (state.selectedColorAdjustTab) {
                        ColorAdjustTab.BRIGHTNESS -> brightness.floatValue
                        ColorAdjustTab.CONTRAST -> contrast.floatValue
                        ColorAdjustTab.SATURATION -> saturation.floatValue
                    },
                    valueRange = when (state.selectedColorAdjustTab) {
                        ColorAdjustTab.BRIGHTNESS -> -255f..255f
                        ColorAdjustTab.CONTRAST -> 0f..10f
                        ColorAdjustTab.SATURATION -> 0f..5f
                    },
                    onValueChangeFinished = {
                        when (state.selectedColorAdjustTab) {
                            ColorAdjustTab.BRIGHTNESS -> {
                                onBrightnessChange(brightness.floatValue)
                            }

                            ColorAdjustTab.CONTRAST -> {
                                onContrastChange(contrast.floatValue)
                            }

                            ColorAdjustTab.SATURATION -> {
                                onSaturationChange(saturation.floatValue)
                            }
                        }
                    },
                    onValueChange = {
                        when (state.selectedColorAdjustTab) {
                            ColorAdjustTab.BRIGHTNESS -> brightness.floatValue = it
                            ColorAdjustTab.CONTRAST -> contrast.floatValue = it
                            ColorAdjustTab.SATURATION -> saturation.floatValue = it
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.1f
                            )
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorAdjustOption(
                        icon = Icons.Rounded.CropFree,
                        title = "Saturation",
                        modifier = Modifier.weight(1f),
                        onClick = { onSaturationSelected() },
                        isSelected = state.selectedColorAdjustTab == ColorAdjustTab.SATURATION,
                    )
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.1f
                        ),
                    )
                    ColorAdjustOption(
                        icon = Icons.Rounded.CropFree,
                        title = "Contrast",
                        modifier = Modifier.weight(1f),
                        onClick = { onContrastSelected() },
                        isSelected = state.selectedColorAdjustTab == ColorAdjustTab.CONTRAST
                    )
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.1f
                        ),
                    )
                    ColorAdjustOption(
                        icon = Icons.Rounded.CropFree,
                        title = "Brightness",
                        modifier = Modifier.weight(1f),
                        onClick = { onBrightnessSelected() },
                        isSelected = state.selectedColorAdjustTab == ColorAdjustTab.BRIGHTNESS
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTab(
    filterUiAnimOffset: Float,
    bitmaps: List<Bitmap>?,
    onFilterSelected: (ImageFilter) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset(y = 100.dp * filterUiAnimOffset)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bitmaps != null) {
                    bitmaps.forEachIndexed { index, bitmap ->
                        Column(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .padding(top = 12.dp, bottom = 4.dp)
                                .clickable {
                                    onFilterSelected(ImageFilter.entries[index])
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = bitmap,
                                modifier = Modifier
                                    .height(65.dp)
                                    .aspectRatio(3 / 4f)
                                    .clip(MaterialTheme.shapes.small),
                                contentDescription = ImageFilter.entries[index].toFormattedString(),
                                filterQuality = FilterQuality.Low,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ImageFilter.entries[index].toFormattedString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }else {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Applying Filters...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorAdjustOption(
    modifier: Modifier = Modifier,
    icon: Any,
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .then(
                if (isSelected) Modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant
                ) else Modifier
            )
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        when (icon) {
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            is Bitmap -> {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.size(20.dp)
                )
            }

            is Int -> {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}


@Composable
fun EditOption(
    icon: Any,
    title: String,
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (icon) {
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            is Bitmap -> {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.size(28.dp)
                )
            }

            is Int -> {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun DocumentEditOptions(
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel,
    currentPage: Int,
    isColorAdjustSelected: Boolean = false,
    isFilterSelected: Boolean = false,
    showColorAdjustUi: () -> Unit = {},
    showFilterUi: () -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    LazyRow(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            EditOption(
                icon = Icons.Rounded.Crop,
                title = "Crop",
                onClick = {
                    viewModel.trySendAction(ScannerAction.EditAction.CropDocument(currentPage))
                },
            )
            EditOption(
                icon = Icons.Rounded.FlipCameraIos,
                title = "Retake",
                onClick = {
                    viewModel.trySendAction(ScannerAction.EditAction.RetakeDocument(currentPage))
                },
            )
            EditOption(
                icon = Icons.AutoMirrored.Rounded.RotateRight,
                title = "Rotate",
                onClick = {
                    viewModel.trySendAction(ScannerAction.EditAction.RotateDocument(currentPage))
                },
            )
            EditOption(
                icon = Icons.Rounded.PhotoFilter,
                title = "Filter",
                isSelected = isFilterSelected,
                onClick = {
                    showFilterUi()
                    //viewModel.trySendAction(ScannerAction.Alert.ShowFilterImageAlert)
                },
            )
            EditOption(
                icon = Icons.Rounded.FilterHdr,
                title = "Adjust",
                onClick = {
                    showColorAdjustUi()
                },
                isSelected = isColorAdjustSelected
            )
            EditOption(
                icon = Icons.Rounded.DeleteOutline,
                title = "Delete",
                onClick = {
                    viewModel.trySendAction(ScannerAction.Alert.DeleteImageAlert)
                },
            )
        }
    }
}

@Composable
fun DocumentEditActions(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSaveDocument: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            modifier = Modifier
                .height(48.dp)
                .weight(1f), onClick = onBack
        ) {
            Text(
                text = "Scan More",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalButton(
            modifier = Modifier
                .height(48.dp)
                .weight(1f),
            onClick = onSaveDocument,
        ) {
            Text(
                text = "Save Pdf",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentNavigator(
    pagerState: PagerState, documents: List<CameraScannedImage>
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        IconButton(modifier = Modifier.align(Alignment.CenterStart), onClick = {
            if (pagerState.currentPage > 0) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        }) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIos,
                contentDescription = "Back",
                tint = if (pagerState.currentPage > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.3f
                )
            )
        }
        Text(
            text = "Document (${pagerState.currentPage + 1}/${documents.size})",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = {
                if (pagerState.currentPage < documents.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "Back",
                tint = if (pagerState.currentPage < documents.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.3f
                )
            )
        }
    }
}

@Composable
private fun ScannedImageView(
    image: Bitmap,
    scannedImageEffect: ImageEffect,
) {
    val zoomable = rememberZoomableState()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .aspectRatio(3 / 4f), contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Document",
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomable),
        )
    }
}

@Composable
fun ShowFilterDialog(
    onDismiss: () -> Unit,
    cachedBitmaps: List<Bitmap>?,
    onCacheBitmap: (List<Bitmap>) -> Unit,
    scannedDocument: CameraScannedImage,
    applyFilterFunction: suspend (Bitmap) -> List<Bitmap>,
    onFilterSelected: (ImageFilter) -> Unit
) {
    val isLoading = remember { mutableStateOf(true) }
    val imageFilters = remember { mutableListOf<Bitmap?>(null) }
    val selectedFilter = remember { mutableStateOf(scannedDocument.imageEffect.imageFilter) }
    LaunchedEffect(Unit) {
        if (cachedBitmaps != null) {
            imageFilters.addAll(cachedBitmaps)
            imageFilters.removeAt(0)
        } else {
            scannedDocument.croppedImage?.let {
                val filteredImages = withContext(Dispatchers.IO) {
                    return@withContext applyFilterFunction(it)
                }
                imageFilters.addAll(filteredImages)
                imageFilters.removeAt(0)
                onCacheBitmap(filteredImages)
            }
        }
        isLoading.value = false
    }
    AppBasicDialog(onDismiss = onDismiss, title = "Filter List", content = {
        key(isLoading.value to selectedFilter.value) {
            if (!isLoading.value) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxHeight(0.7f),
                    columns = GridCells.Adaptive(70.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ImageFilter.entries.size) {
                        val filter = ImageFilter.entries[it]
                        val image = imageFilters.getOrNull(it)
                        Column(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .then(
                                    if (selectedFilter.value == filter) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                    else Modifier
                                )
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clickable {
                                    selectedFilter.value = filter
                                }, horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = image,
                                modifier = Modifier
                                    .aspectRatio(3 / 4f)
                                    .clip(MaterialTheme.shapes.medium),
                                contentDescription = filter.toFormattedString(),
                                filterQuality = FilterQuality.Low,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = filter.toFormattedString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Applying Filters...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }, actions = {
        OutlinedButton(
            onClick = onDismiss
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalButton(onClick = {
            onFilterSelected(selectedFilter.value)
            onDismiss()
        }) {
            Text(
                text = "Apply",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    )
}

@Composable
fun NoDocumentFoundView(
    backToCamera: () -> Unit
) {
    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            TextButton(
                onClick = { backToCamera() },
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "Back Navigation",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.DocumentScanner,
                contentDescription = "Alert Icon",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(150.dp)
                    .alpha(0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Documents Captured",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Try clicking some photos to view here",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}