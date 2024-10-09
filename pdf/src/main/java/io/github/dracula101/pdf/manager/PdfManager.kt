package io.github.dracula101.pdf.manager

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.compose.ui.unit.Dp
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
        fileExtension: String,
        imageQuality: Int,
        resizedHeight: Int,
        onPdfPageAdded: (Int) -> Unit = {}
    ): List<Bitmap>

    suspend fun savePdf(
        files: List<File>,
        file: File,
        imageQuality: Int,
        pdfSize: Size,
        margins: Float,
    ): Boolean

    suspend fun mergePdf(
        files: List<File>,
        file: File,
        imageQuality: Int,
        pdfSize: Size,
        margins: Float,
    ): Boolean

    suspend fun encryptPdf(
        inputFile: File,
        outputFile: File,
        password: String,
        masterPassword: String,
    ): Boolean

    suspend fun decryptPdf(
        uri: Uri,
        outputFile: File,
        contentResolver: ContentResolver,
        password: String
    ): Boolean

    suspend fun pdfHasPassword(
        uri: Uri,
        contentResolver: ContentResolver,
    ): Boolean
}