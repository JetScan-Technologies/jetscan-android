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
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfDocument
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
            val canvas  = Canvas()
            val scale = resizedHeight.toFloat() / page.height
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            canvas.drawColor(Color.White.toArgb())
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val file = File(scannedImageDirectory, "${fileNamePrefix}_$i$fileExtension")
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
            val pdfDocument = Document()
            try {
                val pdfWriter = PdfWriter.getInstance(pdfDocument, file.outputStream())
                pdfDocument.open()
                files.forEach {
                    val image = Image.getInstance(it.absolutePath)
                    if (image.width > pdfSize.width) {
                        val width = pdfSize.width.toFloat()
                        val height = image.height * pdfSize.width / image.width
                        image.scaleToFit(width, height)
                        val left = 0f
                        val bottom = (pdfSize.height - height) / 2
                        image.setAbsolutePosition(left, bottom)
                    } else {
                        val width = image.width
                        val height = image.height
                        image.scaleToFit(width, height)
                        val left = (pdfSize.width - width) / 2
                        val bottom = (pdfSize.height - height) / 2
                        image.setAbsolutePosition(left, bottom)
                    }
                    pdfDocument.add(image)
                    pdfDocument.newPage()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
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
        return withContext(Dispatchers.IO){
            val pdfDocument = Document(PageSize.A4)
            try {
                val pdfWriter = PdfWriter.getInstance(pdfDocument, file.outputStream())
                pdfDocument.open()
                files.forEach {
                    val image = Image.getInstance(it.absolutePath)
                    if (image.width > pdfSize.width) {
                        val width = pdfSize.width.toFloat()
                        val height = image.height * pdfSize.width / image.width
                        image.scaleToFit(width, height)
                        val left = 0f
                        val bottom = (pdfSize.height - height) / 2
                        image.setAbsolutePosition(left, bottom)
                    } else {
                        val width = image.width
                        val height = image.height
                        image.scaleToFit(width, height)
                        val left = (pdfSize.width - width) / 2
                        val bottom = (pdfSize.height - height) / 2
                        image.setAbsolutePosition(left, bottom)
                    }
                    pdfDocument.add(image)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                pdfDocument.close()
            }
        }
    }

    override suspend fun protectPdf(
        file: File,
        password: String,
        masterPassword: String,
        permissions: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pdfReader = PdfReader(file.absolutePath)
                val pdfStamper = PdfStamper(pdfReader, FileOutputStream(file))
                pdfStamper.setEncryption(
                    password.toByteArray(),
                    masterPassword.toByteArray(),
                    permissions,
                    PdfWriter.ENCRYPTION_AES_128
                )
                pdfStamper.close()
                pdfReader.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

}