package io.github.dracula101.jetscan.data.document.manager.pdf

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.Dp
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.doc.DocQuality
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import java.io.File

interface PdfManager {

    fun getPdfPage(file: File, size: Dp, density: Float, index: Int = 0): Bitmap?

    fun savePdfPage(
        contentResolver: ContentResolver,
        uri: Uri,
        name: String,
        index: Int = 0,
        filesPath: String
    ): File?

    fun getImagesFromPdf(contentResolver: ContentResolver, uri: Uri): List<Bitmap>

    fun getPdfPages(contentResolver: ContentResolver, uri: Uri): Int?

    fun getPdfPage(contentResolver: ContentResolver, uri: Uri, index: Int): Bitmap?

    suspend fun loadPdfAsyncPages(
        uri: Uri,
        contentResolver: ContentResolver,
        scannedImageDirectory: File,
        fileNamePrefix: String,
        fileExtension: Extension = Extension.JPEG,
        imageQuality: ImageQuality = ImageQuality.MEDIUM,
        delay: Long = 16,
        onPdfPageAdded: (Int) -> Unit = {}
    ): List<Bitmap>

    suspend fun saveToPdf(
        scannedImages: List<File>,
        file: File,
        pdfQuality: DocQuality
    )

    suspend fun mergePdf(
        images: List<File>,
        file: File,
        pdfQuality: DocQuality
    )


}