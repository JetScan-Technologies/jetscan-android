package io.github.dracula101.jetscan.presentation.features.document.camera


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraInitialized: (LifecycleCameraController, Size) -> Unit,
    imageAnalyzer: ImageAnalysis.Analyzer?,
    cameraViewModel: CameraViewModel = hiltViewModel(),
    aspectRatio: Int = AspectRatio.RATIO_4_3,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val previewSize = remember { mutableStateOf<Size?>(null) }
    val touchCoords = remember { mutableStateOf<Pair<Float, Float>?>(null) }
    val zoomState = remember { mutableStateOf<ZoomState?>(null) }
    val tapFocusState = remember { mutableStateOf<Int?>(null) }
    val isZooming = remember { mutableStateOf(false) }
    val isFocusing = remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(zoomState.value) {
        isZooming.value = true
        handler.postDelayed({
            isZooming.value = false
        }, 1000)
    }

    LaunchedEffect(tapFocusState.value) {
        isFocusing.value = true
        handler.postDelayed({
            isFocusing.value = false
        }, 2000)
    }

    // =================== Animation =====================
    val zoomAnimationOpacity by animateFloatAsState(
        targetValue = if (isZooming.value) 1f else 0f,
        label = "Zoom Animation Opacity"
    )
    val focusScalingAnimation by animateFloatAsState(
        targetValue = when (tapFocusState.value) {
            CameraController.TAP_TO_FOCUS_STARTED -> 100f
            CameraController.TAP_TO_FOCUS_FOCUSED -> 60f
            else -> 45f
        },
        label = "Focus Scaling Animation"
    )
    val focusAnimationOpacity by animateFloatAsState(
        targetValue = when (tapFocusState.value) {
            CameraController.TAP_TO_FOCUS_STARTED -> 1f
            CameraController.TAP_TO_FOCUS_FOCUSED -> 0f
            else -> 0f
        } + if (isFocusing.value) 1f else 0f,
        label = "Focus Animation Opacity"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { context ->
                val view = initPreviewView(context, previewSize)
                view.initializeCameraParams(
                    cameraController = cameraController,
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    aspectRatio = aspectRatio,
                    maximizeQuality = true,
                    imageAnalyzer = imageAnalyzer
                )
                view.initializeCameraListeners(
                    lifecycleOwner = lifecycleOwner,
                    zoomState = zoomState,
                    tapFocusState = tapFocusState,
                    onTouchEvent = { touchCoords.value = it },
                    cameraViewModel = cameraViewModel
                )
                onCameraInitialized.invoke(cameraController, previewSize.value!!)
                view
            },
        )
        if (touchCoords.value != null)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            translationX = touchCoords.value?.first?.minus(30f) ?: 0f
                            translationY = touchCoords.value?.second?.minus(30f) ?: 0f
                            alpha = focusAnimationOpacity
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(focusScalingAnimation.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground,
                                MaterialTheme.shapes.medium
                            )
                    )

                }
            }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = zoomAnimationOpacity
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Slider(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .graphicsLayer {
                            rotationZ = 270f
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                Constraints(
                                    minWidth = constraints.minHeight,
                                    maxWidth = constraints.maxHeight,
                                    minHeight = constraints.minWidth,
                                    maxHeight = constraints.maxHeight,
                                )
                            )
                            layout(placeable.height, placeable.width) {
                                placeable.place(-placeable.width, 0)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    value = zoomState.value?.linearZoom ?: 0f,
                    valueRange = 0f..1f,
                    onValueChange = {
                        cameraController.setLinearZoom(it)
                    },
                )
                Text(
                    text = "x${"%.2f".format((zoomState.value?.linearZoom ?: 0f).times(10f))}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


private fun initPreviewView(
    context: Context,
    previewSize: MutableState<Size?>,
): PreviewView {
    val previewView = PreviewView(context)
    previewView.applyCameraLayoutParams()
    val size = Size(previewView.width.toFloat(), previewView.height.toFloat())
    previewSize.value = size
    return previewView
}

private fun buildPreview(): Preview {
    return Preview
        .Builder()
        .setPreviewStabilizationEnabled(true)
        .setResolutionSelector(buildResolutionSelector( AspectRatio.RATIO_4_3 ))
        .build()
}

private fun PreviewView.initializeCameraListeners(
    lifecycleOwner: LifecycleOwner,
    zoomState: MutableState<ZoomState?>,
    tapFocusState: MutableState<Int?>,
    onTouchEvent: (Pair<Float, Float>) -> Unit,
    cameraViewModel: CameraViewModel,
) {
    controller?.zoomState?.observe(lifecycleOwner) { zoomState.value = it }
    controller?.tapToFocusState?.observe(lifecycleOwner) { tapFocusState.value = it }
    setOnTouchListener { view: View, motionEvent: MotionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchEvent.invoke(
                    Pair(
                        motionEvent.x / 1.17f,
                        motionEvent.y / 1.17f
                    )
                )
                return@setOnTouchListener true
            }

            MotionEvent.ACTION_UP -> {
                val factory = meteringPointFactory
                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                val action = FocusMeteringAction.Builder(point).build()
                controller?.cameraControl?.startFocusAndMetering(action)?.addListener(
                    { view.performClick() },
                    ContextCompat.getMainExecutor(context)
                )
                return@setOnTouchListener true
            }

            else -> return@setOnTouchListener false
        }
    }
    controller?.cameraInfo?.cameraState?.observe(lifecycleOwner) {
        cameraViewModel.onCameraStateEvent(it)
    }
}


private fun PreviewView.applyCameraLayoutParams() {
    layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    setBackgroundColor(Color.Black.toArgb())
    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    scaleType = PreviewView.ScaleType.FIT_CENTER
}

private fun PreviewView.initializeCameraParams(
    cameraController: LifecycleCameraController,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    @AspectRatio.Ratio
    aspectRatio: Int = AspectRatio.RATIO_4_3,
    maximizeQuality: Boolean,
    imageAnalyzer: ImageAnalysis.Analyzer?,
) {
    // ================ Camera Controller ================
    controller = cameraController

    // ================ Camera Layout ================
    cameraController.imageCaptureResolutionSelector = buildResolutionSelector(aspectRatio)
    cameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
    cameraController.imageCaptureMode = getCameraQuality(maximizeQuality)
    cameraController.imageCaptureIoExecutor = ContextCompat.getMainExecutor(context)

    // ================ Camera Preview ================
    cameraController.previewResolutionSelector = buildResolutionSelector(aspectRatio)

    cameraController.bindToLifecycle(lifecycleOwner)

    // ================ Image Analysis ================
    imageAnalyzer?.let {
        initializeImageAnalysis(context, aspectRatio)
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            it,
        )
    }
}

private fun getCameraQuality(maximizeQuality: Boolean): Int {
    return if (maximizeQuality) {
        ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    } else {
        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    }
}

private fun buildImageCapture(executor: Executor): ImageCapture {
    return ImageCapture
        .Builder()
        .setCaptureMode(getCameraQuality(false))
        .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
        .setTargetRotation(Surface.ROTATION_0)
        .setIoExecutor(executor)
        .build()
}

fun PreviewView.initializeImageAnalysis(
    context: Context,
    @AspectRatio.Ratio aspectRatio: Int
) {
    controller?.imageAnalysisBackgroundExecutor = ContextCompat.getMainExecutor(context)
    controller?.imageAnalysisResolutionSelector = buildResolutionSelector(aspectRatio)
    controller?.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    controller?.imageAnalysisImageQueueDepth = 1
    controller?.imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
}

private fun buildResolutionSelector(aspectRatio: Int): ResolutionSelector {
    return ResolutionSelector
        .Builder()
        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
        .setAspectRatioStrategy(
            if (aspectRatio == AspectRatio.RATIO_16_9) {
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
            } else {
                AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
            }
        )
        .setAllowedResolutionMode(ResolutionSelector.PREFER_CAPTURE_RATE_OVER_HIGHER_RESOLUTION)
        .build()
}