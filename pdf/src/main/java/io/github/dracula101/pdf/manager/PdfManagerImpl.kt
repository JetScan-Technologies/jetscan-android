package io.github.dracula101.pdf.manager

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.ObjectFit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
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
        file: File,
        imageQuality: Int,
        pdfSize: Size,
        margins: Float
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            document.setMargins(margins, margins, margins, margins)
            try {
                files.forEachIndexed { index, it ->
                    val imageFactory = ImageDataFactory.create(it.absolutePath)
                    val image = Image(imageFactory)
                    val imageWidth = image.imageWidth
                    val imageHeight = image.imageHeight
                    image.setMargins(margins, margins, margins, margins)
                    image.setPadding(0f)
                    if(imageWidth > imageHeight) {
                        image.setMinWidth(pdfSize.width - margins * 2)
                        val top = (pdfSize.height - imageHeight * pdfSize.width / imageWidth - margins) / 2
                        image.setRelativePosition(margins, top, 0f, 0f)
                    } else {
                        image.setMaxHeight(pdfSize.height - margins * 2)
                    }
                    image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    image.objectFit = ObjectFit.CONTAIN
                    document.add(image)
                    if (index < files.size - 1) {
                        pdfDocument.addNewPage()
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                document.close()
                pdfDocument.close()
            }
        }
    }

    override suspend fun mergePdf(
        files: List<File>,
        file: File,
        imageQuality: Int,
        pdfSize: Size,
        margins: Float
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            try {
                files.forEach {
                    val imageFactory = ImageDataFactory.create(it.absolutePath)
                    val image = Image(imageFactory)
                    image.setMargins(margins, margins, margins, margins)
                    image.setPadding(0f)
                    val imageWidth = image.imageWidth
                    val imageHeight = image.imageHeight
                    if(imageWidth > imageHeight) {
                        image.setMinWidth(pdfSize.width - margins * 2)
                        val top = (pdfSize.height - imageHeight * pdfSize.width / imageWidth - margins) / 2
                        image.setRelativePosition(margins, top, 0f, 0f)
                    } else {
                        image.setMaxHeight(pdfSize.height - margins * 2)
                    }
                    image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    image.objectFit = ObjectFit.CONTAIN
                    document.add(image)
                    if (files.indexOf(it) < files.size - 1) {
                        pdfDocument.addNewPage()
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                document.close()
                pdfDocument.close()
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
                val pdfReader = PdfReader(inputFile)
                val writerProperties = WriterProperties()
                writerProperties.setStandardEncryption(
                    password.toByteArray(),
                    masterPassword.toByteArray(),
                    EncryptionConstants.ALLOW_PRINTING,
                    EncryptionConstants.ENCRYPTION_AES_128 or EncryptionConstants.DO_NOT_ENCRYPT_METADATA
                )
                val pdfWriter = PdfWriter(outputFile.outputStream(), writerProperties)
                val pdfDocument = PdfDocument(pdfReader, pdfWriter)
                pdfDocument.close()
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
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val pdfReader = PdfReader(
                        inputStream,
                        ReaderProperties()
                            .setPassword(password.toByteArray())
                    )
                    val pdfWriter = PdfWriter(outputFile)
                    val pdfDocument = PdfDocument(pdfReader, pdfWriter)
                    pdfDocument.close()
                    Log.d("PdfManagerImpl", "Decrypted file: ${outputFile.absolutePath}, size: ${outputFile.length()}")
                }
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
            } catch (e: SecurityException) {
                true
            } finally {
                inputTempFile.delete()
            }
        }
    }

}