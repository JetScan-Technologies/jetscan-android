package io.github.dracula101.jetscan.presentation.features.document.ocr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.component.appbar.JetScanTopAppbar
import io.github.dracula101.jetscan.presentation.platform.component.bottomsheet.AppBottomSheet
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OcrScreen(
    onNavigate: () -> Unit,
    documentId: String,
    documentName: String?,
    pageIndex: Int?,
    viewModel: OcrViewModel = hiltViewModel(),
){
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val lazyListScrollState = rememberLazyListState()
    val scanningAnimationComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.doc_scan_overlay_anim)
    )
    val bottomSheetState = rememberModalBottomSheetState()
    val clipBoardManager = LocalClipboardManager.current

    LaunchedEffect(Unit){
        viewModel.trySendAction(OcrAction.Internal.LoadDocument(documentId, pageIndex ?: 0))
    }
    LaunchedEffect(state.value.ocrResult){
        if(state.value.ocrResult != null){
            scope.launch { bottomSheetState.show() }
        }
    }

    if(bottomSheetState.isVisible){
        AppBottomSheet(
            onDismiss = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ){
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OCR Result",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            clipBoardManager.setText(
                                AnnotatedString(state.value.ocrResult?.text ?: "")
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy OCR Result",
                        )
                        Text(
                            text = "Copy",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SelectionContainer {
                    Text(
                        text = state.value.ocrResult?.text ?: "No OCR Result",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    JetScanScaffold(
        topBar = {
            OcrTopAppBar(
                onNavigate = onNavigate,
                documentName = documentName ?: state.value.document?.name,
                scrollBehavior = scrollBehavior
            )
        },
        alwaysShowBottomBar = true,
        bottomBar = {
            if(!state.value.isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ){
                    ElevatedButton(
                        onClick = {
                            scope.launch { bottomSheetState.show() }
                        },
                        modifier = Modifier.padding(16.dp)
                    ){
                        Text(
                            text = "View OCR Result",
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding, scaffoldSize->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(
                    vertical = 8.dp
                ),
            state = lazyListScrollState,
        ){
            item {
                Text(
                    text = "OCR",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = if(state.value.isLoading) "Processing OCR..." else "Tap on the image to view OCR annotations",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item{
                if(state.value.document != null){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight()
                                .clickable {
                                    if (!state.value.isLoading) {
                                        scope.launch {
                                            bottomSheetState.show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ){
                                val primaryColor = MaterialTheme.colorScheme.primary
                                val image = state.value.document?.scannedImages?.getOrNull(pageIndex ?: 0)
                                AsyncImage(
                                    model = state.value.document?.scannedImages?.getOrNull(
                                        pageIndex ?: 0
                                    )?.scannedUri,
                                    contentDescription = "Scanned Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(
                                            ratio = image?.width
                                                ?.toFloat()
                                                ?.div(image.height) ?: 1f
                                        )
                                )
                                if(state.value.ocrResult != null){
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(
                                                ratio = image?.width
                                                    ?.toFloat()
                                                    ?.div(image.height) ?: 1f
                                            ),
                                    ){
                                        drawPath(
                                            path = Path().apply {
                                                moveTo(0f, 0f)
                                                lineTo(size.width, 0f)
                                                lineTo(size.width, size.height)
                                                lineTo(0f, size.height)
                                                close()
                                            },
                                            color = primaryColor,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                        state.value.ocrResult?.pages?.first()?.annotations?.forEach { annotation ->
                                            val path = Path().apply {
                                                val topLeft = Offset(
                                                    x = (annotation.boundingBox?.vertices?.get(0)?.normX ?: 0f) * size.width,
                                                    y = (annotation.boundingBox?.vertices?.get(0)?.normY ?: 0f) * size.height
                                                )
                                                val topRight = Offset(
                                                    x = (annotation.boundingBox?.vertices?.get(1)?.normX ?: 0f) * size.width,
                                                    y = (annotation.boundingBox?.vertices?.get(1)?.normY ?: 0f) * size.height
                                                )
                                                val bottomRight = Offset(
                                                    x = (annotation.boundingBox?.vertices?.get(2)?.normX ?: 0f) * size.width,
                                                    y = (annotation.boundingBox?.vertices?.get(2)?.normY ?: 0f) * size.height
                                                )
                                                val bottomLeft = Offset(
                                                    x = (annotation.boundingBox?.vertices?.get(3)?.normX ?: 0f) * size.width,
                                                    y = (annotation.boundingBox?.vertices?.get(3)?.normY ?: 0f) * size.height
                                                )
                                                moveTo(topLeft.x, topLeft.y)
                                                lineTo(topRight.x, topRight.y)
                                                lineTo(bottomRight.x, bottomRight.y)
                                                lineTo(bottomLeft.x, bottomLeft.y)
                                                close()
                                            }
                                            drawPath(
                                                path = path,
                                                color = primaryColor,
                                                style = Stroke(width = 0.5.dp.toPx())
                                            )
                                            drawPath(
                                                path = path,
                                                color = primaryColor.copy(alpha = 0.1f),
                                                style = Fill
                                            )
                                        }
                                    }

                                }
                            }
                            if(state.value.isLoading){
                                LottieAnimation(
                                    composition = scanningAnimationComposition,
                                    iterations = LottieConstants.IterateForever,
                                    speed = 0.75f,
                                    modifier = Modifier
                                        .scale(1.10f)
                                )
                            }
                        }
                    }
                }
            }
            if(state.value.document != null){
                item{
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .height(90.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                        ){
                            state.value.document?.scannedImages?.forEachIndexed { index, scannedImage ->
                                AsyncImage(
                                    model = scannedImage.scannedUri,
                                    contentDescription = "Scanned Image $index",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .then(
                                            if (index == pageIndex) {
                                                Modifier
                                                    .border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                                    .padding(2.dp)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrTopAppBar(
    onNavigate: () -> Unit,
    documentName: String?,
    scrollBehavior: TopAppBarScrollBehavior
){
    JetScanTopAppbar(
        scrollBehavior = scrollBehavior,
        onNavigationIconClick = onNavigate,
        title = {
            Text(
                text = documentName ?: "OCR",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )

}