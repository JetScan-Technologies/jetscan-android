package io.github.dracula101.jetscan.presentation.platform.feature.scanner.imageproxy

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Size
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.utils.rotate
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class DocumentAnalyzer(
    private val openCvManager: OpenCvManager,
    private val imagePreviewSize: Size,
    private val onDocumentDetected: (ImageCropCoords) -> Unit,
    private val onPreviewBitmapReady: (Bitmap, Bitmap) -> Unit = { _, _ -> }
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun analyze(image: ImageProxy) {
        scope.launch {
            image.use { imageProxy ->
                val imageBitmap = imageProxy.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                val imageCropCoords = openCvManager.detectSingleDocument(
                    imageBitmap = imageBitmap,
                    onPreviewBitmapReady = onPreviewBitmapReady,
                    imageSize = imagePreviewSize,
                )
                imageCropCoords?.let {
                    onDocumentDetected(it)
                }
                delay(THROTTLE_DELAY)
            }
        }.invokeOnCompletion {
            if (it != null) {
                Timber.e(it)
            }
        }
    }

    companion object {
        const val THROTTLE_DELAY = 200L
    }
}
