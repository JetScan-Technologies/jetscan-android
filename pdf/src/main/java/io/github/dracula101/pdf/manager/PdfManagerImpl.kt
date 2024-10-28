package io.github.dracula101.pdf.manager

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfStream
import com.itextpdf.text.pdf.PdfWriter
import io.github.dracula101.pdf.models.PdfCompressionLevel
import io.github.dracula101.pdf.models.PdfOptions
import io.github.dracula101.pdf.models.PdfQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PdfManagerImpl : PdfManager {

    override fun getPdfPage(file: File, size: Dp, density: Float, index: Int): Bitmap? {
        var pdfBitmap: Bitmap? = null
        val parcelFileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        if (parcelFileDescriptor != null) {
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(index)
                val width = (size.value * density).toInt()
                val height = (width * page.height / page.width.toFloat()).toInt()
                pdfBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(pdfBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
            }
            pdfRenderer.close()
            parcelFileDescriptor.close()
        }

        return pdfBitmap
    }

    override fun savePdfPage(
        contentResolver: ContentResolver,
        uri: Uri,
        name: String,
        index: Int,
        filesPath: String
    ): File? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            val page = pdfRenderer.openPage(index)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val file = File(filesPath, name.substringBeforeLast(".").plus(".png"))
            val outputStream = FileOutputStream(file)
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                95,
                outputStream
            )
            outputStream.flush()
            page.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun getImagesFromPdf(contentResolver: ContentResolver, uri: Uri): List<Bitmap> {
        val images = mutableListOf<Bitmap>()
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                images.add(bitmap)
                page.close()
            }
            pdfRenderer.close()
            parcelFileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return images
    }

    override fun getPdfPages(contentResolver: ContentResolver, uri: Uri): Int? {
        return try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            val pageCount = pdfRenderer.pageCount
            pdfRenderer.close()
            parcelFileDescriptor.close()
            pageCount
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getPdfPage(contentResolver: ContentResolver, uri: Uri, index: Int): Bitmap? {
        return try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            val page = pdfRenderer.openPage(index)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadPdfAsyncPages(
        uri: Uri,
        contentResolver: ContentResolver,
        scannedImageDirectory: File,
        fileNamePrefix: String,
        fileExtension: String,
        imageQuality: Int,
        resizedHeight: Int,
        onPdfPageAdded: (Int) -> Unit
    ): List<Bitmap> = coroutineScope {
        val images = mutableListOf<Bitmap>()
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return@coroutineScope images
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            val canvas = Canvas()
            val scale = resizedHeight.toFloat() / page.height
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            canvas.drawColor(Color.White.toArgb())
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val file = File(scannedImageDirectory, "${fileNamePrefix}_$i.$fileExtension")
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, it)
            }
            images.add(bitmap)
            onPdfPageAdded(i)
            page.close()
        }
        pdfRenderer.close()
        parcelFileDescriptor.close()
        images
    }

    override suspend fun savePdf(
        files: List<File>,
        output: File,
        options: PdfOptions,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val pdfDocument = Document()
            val pdfWriter = PdfWriter.getInstance(pdfDocument, FileOutputStream(output))
            try {
                pdfWriter.compressionLevel = when(options.quality) {
                    PdfQuality.VERY_LOW -> PdfStream.BEST_COMPRESSION
                    PdfQuality.LOW -> PdfStream.BEST_COMPRESSION
                    PdfQuality.MEDIUM -> PdfStream.DEFAULT_COMPRESSION
                    PdfQuality.HIGH -> PdfStream.NO_COMPRESSION
                }
                if(options.imageQuality < 50){
                    pdfWriter.setFullCompression()
                }
                val margin = if (options.hasMargin) 20f else 0f
                pdfDocument.setMargins(margin, margin, margin, margin)
                pdfDocument.open()
                pdfDocument.setPageCount(files.size)
                pdfDocument.setPageSize(Rectangle(options.pageSize.width, options.pageSize.height))
                pdfDocument.setMarginMirroring(false)
                pdfDocument.setMarginMirroringTopBottom(false)
                files.forEachIndexed { index, file ->
                    val fileOutputStream = ByteArrayOutputStream()
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options.imageQuality, fileOutputStream)
                    val image = Image.getInstance(fileOutputStream.toByteArray())
                    image.scaleToFit(options.pageSize.width, options.pageSize.height)
                    image.alignment = Image.ALIGN_CENTER
                    image.setAbsolutePosition(
                        (options.pageSize.width - image.scaledWidth) / 2,
                        (options.pageSize.height - image.scaledHeight) / 2
                    )
                    pdfDocument.add(image)
                    if (index < files.size - 1) {
                        pdfDocument.newPage()
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                pdfDocument.close()
                pdfWriter.close()
            }

        }
    }

    override suspend fun encryptPdf(
        inputFile: File,
        outputFile: File,
        password: String,
        masterPassword: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pdfReader = PdfReader(inputFile.absolutePath)
                val pdfStamper = PdfStamper(pdfReader, outputFile.outputStream())
                pdfStamper.setEncryption(
                    password.toByteArray(),
                    masterPassword.toByteArray(),
                    PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                    PdfWriter.ENCRYPTION_AES_128 or PdfWriter.DO_NOT_ENCRYPT_METADATA
                )
                pdfStamper.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun decryptPdf(uri: Uri, outputFile: File, contentResolver: ContentResolver, password: String): Boolean {
        return withContext(Dispatchers.IO){
            try {
                val pdfReader = PdfReader(
                    contentResolver.openInputStream(uri),
                    password.toByteArray()
                )
                Log.d("PdfManagerImpl", "Password: ${pdfReader.computeUserPassword().decodeToString()}")
                val pdfStamper = PdfStamper(pdfReader, outputFile.outputStream())
                pdfStamper.close()
                pdfReader.close()
                val isDecrypted = pdfHasPassword(outputFile.toUri(), contentResolver)
                !isDecrypted
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun pdfHasPassword(uri: Uri, contentResolver: ContentResolver): Boolean {
        return withContext(Dispatchers.IO){
            val inputTempFile = File.createTempFile("temp_locked", ".pdf")
            try {
                inputTempFile.outputStream().use { outputStream ->
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(inputTempFile, ParcelFileDescriptor.MODE_READ_ONLY))
                pdfRenderer.close()
                false
            } catch (e: Exception) {
                if (e is SecurityException) {
                    true
                } else {
                    e.printStackTrace()
                    false
                }
            } finally {
                inputTempFile.delete()
            }
        }
    }

}