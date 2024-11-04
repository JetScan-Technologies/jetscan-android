package io.github.dracula101.jetscan.presentation.features.document.edit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CropOriginal
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Rotate90DegreesCw
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.platform.utils.bytesToSizeAndUnit
import io.github.dracula101.jetscan.presentation.features.document.edit.components.DocumentImage
import io.github.dracula101.jetscan.presentation.features.document.edit.components.EditPdfActionTile
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.text.FittedText
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import io.github.dracula101.jetscan.data.platform.utils.aspectRatio
import io.github.dracula101.jetscan.presentation.features.document.scanner.PointPosition
import io.github.dracula101.jetscan.presentation.features.document.scanner.calculateNewImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.composition.LocalFileActionManager
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.detectTransformGestures
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.CornerPointVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.HolderVisibility
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.toCropOverlay
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph.CPoint
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditDocumentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPdf: (Document) -> Unit,
    documentId: String,
    documentPageIndex: Int,
    onNavigateToOcr: (Document, Int) -> Unit,
    viewModel: EditDocViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.trySendAction(EditDocAction.LoadDocument(documentId, documentPageIndex))
    }
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        pageCount = { state.value.scannedDocument?.scannedImages?.size ?: 0 },
        initialPage = documentPageIndex
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val widthAnimation = remember { Animatable(1f) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fileActionManger = LocalFileActionManager.current
    val imageCropCoords = remember { mutableStateOf<ImageCropCoords?>(null) }
    val boundary = remember { mutableStateOf<Size?>(null) }
    val currentPositionCoords = remember { mutableStateOf<CPoint?>(null) }
    val currentPointPosition = remember { mutableStateOf<PointPosition?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit){
        snapshotFlow { pagerState.currentPage }.collect {
            viewModel.trySendAction(EditDocAction.Ui.ChangeDocumentIndex(it))
        }
    }

    BackHandler(enabled = state.value.isLoading.not() && state.value.view != EditDocView.PREVIEW) {
        if(state.value.view != EditDocView.PREVIEW){
            coroutineScope.launch {
                widthAnimation.animateTo(1f)
                viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.PREVIEW))
            }
        }else {
            coroutineScope.launch {
                rotation.animateTo(0f)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if(state.value.isEdited){
                viewModel.trySendAction(EditDocAction.SaveDocument)
            }
        }
    }

    JetScanScaffold(
        topBar = {
            JetScanTopAppbar(
                title = { Text("Edit Document") },
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = onNavigateBack,
            )
        },
        bottomBar = {
            if (state.value.view != EditDocView.PREVIEW){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ){
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    Row(
                        modifier = Modifier
                            .height(54.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        when(state.value.view){
                            EditDocView.PREVIEW -> {}
                            EditDocView.CROP -> {
                                BottombarItem(
                                    icon = Icons.Rounded.Done,
                                    title = "Save",
                                    onClick = {
                                        if (imageCropCoords.value != null){
                                            val imageDocument = state.value.scannedDocument?.scannedImages?.getOrNull(pagerState.currentPage)
                                            if (imageDocument != null && boundary.value != null){
                                                val widthRatio = (imageDocument.width / boundary.value!!.width)
                                                val heightRatio = (imageDocument.height / boundary.value!!.height)
                                                val cropCoords = imageCropCoords.value!!.scale(widthRatio, heightRatio)
                                                viewModel.trySendAction(EditDocAction.Ui.CropDocument(cropCoords))
                                                coroutineScope.launch{
                                                    widthAnimation.animateTo(1f)
                                                    viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.PREVIEW))
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                VerticalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                )
                                BottombarItem(
                                    icon = Icons.Rounded.Clear,
                                    title = "Cancel",
                                    onClick = {
                                        coroutineScope.launch {
                                            widthAnimation.animateTo(1f)
                                            viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.PREVIEW))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                )
                            }
                            EditDocView.ROTATE -> {
                                BottombarItem(
                                    icon = Icons.AutoMirrored.Rounded.RotateLeft,
                                    title = "Rotate Left",
                                    onClick = {
                                        coroutineScope.launch {
                                            rotation.animateTo(rotation.value - 90f)
                                            viewModel.trySendAction(EditDocAction.Ui.RotateDocument(-90))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                VerticalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                )
                                BottombarItem(
                                    icon = Icons.AutoMirrored.Rounded.RotateRight,
                                    title = "Rotate Right",
                                    onClick = {
                                        coroutineScope.launch {
                                            rotation.animateTo(rotation.value + 90f)
                                            viewModel.trySendAction(EditDocAction.Ui.RotateDocument(+90))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                VerticalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                )
                                BottombarItem(
                                    icon = Icons.Rounded.Close,
                                    onClick = {
                                        coroutineScope.launch {
                                            widthAnimation.animateTo(1f)
                                            viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.PREVIEW))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(0.4f)
                                )
                            }
                            EditDocView.FILTER -> {}
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                }
            }
        }
    ) { padding, windowsize ->
        if (state.value.scannedDocument != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
            ){
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(padding),
                ){
                    VerticalPager(
                        state = pagerState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        userScrollEnabled = state.value.view == EditDocView.PREVIEW,
                        contentPadding = PaddingValues(
                            if (state.value.view == EditDocView.PREVIEW) 0.dp else 16.dp
                        ),
                        modifier = Modifier
                    ) { index ->
                        val image = state.value.scannedDocument!!.scannedImages[index]
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ){
                            DocumentImage(
                                uri = image.scannedUri,
                                canZoom = state.value.view == EditDocView.PREVIEW,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(
                                        if (state.value.view == EditDocView.PREVIEW) 0f else rotation.value
                                    )
                            )
                            if (state.value.view == EditDocView.CROP){
                                if (currentPointPosition.value != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(image.width / image.height.toFloat())
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
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .aspectRatio(image.width / image.height.toFloat())
                                ) {
                                    boundary.value = Size(
                                        constraints.maxWidth.toFloat(),
                                        constraints.maxHeight.toFloat()
                                    )
                                    val imageSpaceSize = Size(
                                        constraints.maxWidth.toFloat(),
                                        constraints.maxHeight.toFloat()
                                    )
                                    if (imageCropCoords.value == null && state.value.view == EditDocView.CROP){
                                        imageCropCoords.value = ImageCropCoords.fromSize(imageSpaceSize)
                                    }
                                    Canvas(
                                        modifier = Modifier
                                            .aspectRatio(image.width / image.height.toFloat())
                                            .pointerInput(Unit) {
                                                detectTransformGestures(
                                                    onGestureStart = { pointerInputChange ->
                                                        val newImageCropCoords =
                                                            calculateNewImageCropCoords(
                                                                imageCropCoords = imageCropCoords.value
                                                                    ?: ImageCropCoords.NONE,
                                                                currentLockedPoint = currentPointPosition.value,
                                                                inputChange = pointerInputChange,
                                                                onPositionChange = { pointPosition ->
                                                                    currentPointPosition.value =
                                                                        pointPosition
                                                                },
                                                                boundary = boundary.value
                                                            )
                                                        imageCropCoords.value =
                                                            newImageCropCoords
                                                        currentPositionCoords.value =
                                                            CPoint(
                                                                pointerInputChange.position.x.toDouble(),
                                                                pointerInputChange.position.y.toDouble()
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
                                                    onGestureEnd = { pointerInputChange ->
                                                        val newImageCropCoords =
                                                            calculateNewImageCropCoords(
                                                                imageCropCoords = imageCropCoords.value
                                                                    ?: ImageCropCoords.NONE,
                                                                currentLockedPoint = currentPointPosition.value,
                                                                inputChange = pointerInputChange,
                                                                onPositionChange = {
                                                                    currentPointPosition.value =
                                                                        null
                                                                },
                                                                boundary = boundary.value
                                                            )
                                                        imageCropCoords.value =
                                                            newImageCropCoords
                                                        currentPointPosition.value = null
                                                    }
                                                )
                                            }
                                    ){
                                        imageCropCoords.value?.let{ coords ->
                                            toCropOverlay(
                                                imageCropCoords = coords,
                                                primaryColor = primaryColor,
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
                                }
                            }
                        }
                    }
                    if(state.value.view == EditDocView.PREVIEW){
                        Row(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .align(Alignment.BottomCenter)
                        ){
                            Text(
                                text = "Page ${pagerState.currentPage + 1} of ${state.value.scannedDocument!!.scannedImages.size}",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    if (widthAnimation.value == 0f && state.value.view == EditDocView.PREVIEW){
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    widthAnimation.animateTo(1f)
                                }
                            },
                            modifier = Modifier
                                .offset(x = 32.dp)
                                .align(Alignment.CenterEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .offset(x = (-8).dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Open sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (widthAnimation.value == 1f){
                    Box(
                        modifier = Modifier
                            .offset(x = (150 * (1 - widthAnimation.value)).dp)
                            .width(60.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .align(Alignment.TopCenter)
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EditPdfActionTile(
                                title = "Crop",
                                icon = Icons.Rounded.CropOriginal,
                                onClick = {
                                    coroutineScope.launch {
                                        widthAnimation.animateTo(0f)
                                        viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.CROP))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Rotate",
                                icon = Icons.Rounded.Rotate90DegreesCw,
                                onClick = {
                                    coroutineScope.launch {
                                        widthAnimation.animateTo(0f)
                                        viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.ROTATE))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // HorizontalDivider(
                            //     color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            // )
                            // EditPdfActionTile(
                            //     title = "Filter",
                            //     icon = Icons.Rounded.Filter,
                            //     onClick = {
                            //         coroutineScope.launch {
                            //             widthAnimation.animateTo(0f)
                            //             viewModel.trySendAction(EditDocAction.Ui.ChangeView(EditDocView.PREVIEW))
                            //         }
                            //     },
                            //     modifier = Modifier.fillMaxWidth()
                            // )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Open PDF",
                                icon = Icons.Rounded.PictureAsPdf,
                                onClick = {
                                    if (state.value.scannedDocument != null) {
                                        onNavigateToPdf(state.value.scannedDocument!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            // EditPdfActionTile(
                            //     title = "Rename",
                            //     icon = Icons.Rounded.Edit,
                            //     onClick = { },
                            //     modifier = Modifier.fillMaxWidth()
                            // )
                            // VerticalDivider(
                            //     modifier = Modifier
                            //         .height(bottomBarHeight * 0.8f),
                            //     color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            // )
                            // EditPdfActionTile(
                            //     title = "Add",
                            //     icon = Icons.Rounded.Rotate90DegreesCw,
                            //     onClick = { },
                            //     modifier = Modifier.width(60.dp).padding(8.dp)
                            // )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "Share",
                                icon = Icons.Rounded.IosShare,
                                onClick = {
                                    val fileUri = FileProvider.getUriForFile(
                                        context,
                                        context.applicationContext.packageName + ".provider",
                                        state.value.scannedDocument!!.scannedImages[pagerState.currentPage].scannedUri.toFile()
                                    )
                                    fileActionManger.shareFile(
                                        fileUri,
                                        title = "Share Image",
                                        subject = state.value.scannedDocument!!.name + " Page ${pagerState.currentPage + 1}",
                                        onActivityNotFound = {}
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                            EditPdfActionTile(
                                title = "OCR",
                                icon = Icons.Rounded.DocumentScanner,
                                onClick = {
                                    onNavigateToOcr(state.value.scannedDocument!!, pagerState.currentPage)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        ),
                                        endY = 110f,
                                    ),
                                )
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        widthAnimation.animateTo(0f)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = "Close"
                                )
                            }
                            FittedText(
                                text = state.value.scannedDocument!!.scannedImages[pagerState.currentPage].size.bytesToSizeAndUnit().first.toString(),
                                fontWeight = FontWeight.ExtraLight,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                            )
                            Text(
                                state.value.scannedDocument!!.scannedImages[pagerState.currentPage].size.bytesToSizeAndUnit().second,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        }
    }


    if (state.value.isLoading){
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerAnimation(
    pagerState: PagerState,
    thisPageIndex: Int,
): Modifier {
    val pageOffset =
        (pagerState.currentPage - thisPageIndex) + pagerState.currentPageOffsetFraction

    return this then Modifier.graphicsLayer {
        alpha =
            lerp(
                start = 0.3f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
            )

        lerp(
            start = 0.9f,
            stop = 1f,
            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
        ).also { scale ->
            scaleX = scale
            scaleY = scale
        }
    }
}

@Composable
fun BottombarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String? = null,
    onClick: () -> Unit,
){
    Row (
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(24.dp)
        )
        if(title != null){
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

}