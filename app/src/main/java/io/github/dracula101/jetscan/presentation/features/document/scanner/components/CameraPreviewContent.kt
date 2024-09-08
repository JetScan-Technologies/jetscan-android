package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import io.github.dracula101.jetscan.presentation.features.document.camera.CameraPreview
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraAspectRatio


@Composable
fun CameraPreviewContent(
    onCameraInitialized: (LifecycleCameraController) -> Unit,
    imageAnalyzer: () -> ImageAnalysis.Analyzer?,
    onPreviewSize: (Size) -> Unit,
    gridStatus: Boolean,
    previewAspectRatio: CameraAspectRatio,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        propagateMinConstraints = true
    ) {
        CameraPreview(
            onCameraInitialized = { controller, _ ->
                onCameraInitialized(controller)
                onPreviewSize(Size(maxWidth.value * density, maxHeight.value * density))
            },
            imageAnalyzer = imageAnalyzer(),
            aspectRatio = previewAspectRatio.toAspectRatio(),
            modifier = Modifier
                .width(maxWidth)
                .aspectRatio(previewAspectRatio.toFloat())
        )
        AnimatedContent(
            targetState = gridStatus,
            label = "Grid Animation",
            transitionSpec = {
                (scaleIn(initialScale = 10f) + fadeIn()) togetherWith (scaleOut(targetScale = 10f) + fadeOut())
            }
        ) { gridStatus ->
            if (gridStatus) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(previewAspectRatio.toFloat())
                        .drawWithContent {
                            val gridRow = 3
                            val gridColumn = 3
                            // ========== Draw Horizontal Lines ==========
                            val horizontalLineSpacing = size.height / gridRow
                            for (i in 1 until gridRow) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(0f, i * horizontalLineSpacing),
                                    end = Offset(size.width, i * horizontalLineSpacing),
                                    strokeWidth = 1f
                                )
                            }

                            // ========== Draw Vertical Lines ==========
                            val verticalLineSpacing = size.width / gridColumn
                            for (i in 1 until gridColumn) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(i * verticalLineSpacing, 0f),
                                    end = Offset(i * verticalLineSpacing, size.height),
                                    strokeWidth = 1f
                                )
                            }
                        }
                )
            }
        }
    }
}