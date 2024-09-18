package io.github.dracula101.jetscan.presentation.platform.feature.scanner.imageproxy

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.utils.rotate
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.image.ImageCropCoords
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.extensions.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class DocumentAnalyzer(
    private val openCvManager: OpenCvManager,
    private val onDocumentDetected: (ImageCropCoords?) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun analyze(image: ImageProxy) {
        scope.launch {
            image.use { imageProxy ->
                val imageBitmap = imageProxy.toBitmap().rotate(90f)
                val reScaleHeight = (imageBitmap.height * IMAGE_WIDTH) / imageBitmap.width
                val reScaledBitmap = Bitmap.createScaledBitmap(imageBitmap, IMAGE_WIDTH, reScaleHeight, true)
                val autoCropCoords = openCvManager.detectDocument(reScaledBitmap)
                onDocumentDetected(
                    autoCropCoords?.scale(
                        scale = (imageProxy.width.toFloat() / IMAGE_WIDTH) * 1/3f
                    )
                )
                imageBitmap.recycle()
            }
        }.invokeOnCompletion {
            if (it != null) {
                Timber.e(it)
            }
        }
    }

    companion object {
        const val THROTTLE_DELAY = 0L
        const val IMAGE_WIDTH = 250
    }
}
