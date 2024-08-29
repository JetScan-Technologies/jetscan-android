package io.github.dracula101.jetscan.presentation.platform.feature.scanner.imageproxy

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.common.api.ApiException
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class BarcodeAnalyzer(
    private val onCodeDetected: (barcode: Barcode) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions
        .Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .enableAllPotentialBarcodes()
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            imageProxy.use { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    val process = scanner.process(image)
                    val barCode = process.await()
                    val barcode = barCode.firstOrNull()
                    if (barcode?.rawValue != null && (barcode.rawValue?.isNotEmpty() == true)) {
                        onCodeDetected(barcode)
                    }
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

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
}

