package io.github.dracula101.jetscan.data.document.manager.pdf

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.unit.Dp
import io.github.dracula101.jetscan.data.document.models.doc.DocQuality
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.getImageHeight
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
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
                ImageQuality.HIGH.toBitmapQuality(),
                outputStream
            )
            outputStream.flush()
            page.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()
            return file
        } catch (e: Exception) {
            Timber.e(e)
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
            Timber.e(e)
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
            Timber.e(e)
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
            Timber.e(e)
            null
        }
    }

    override suspend fun loadPdfAsyncPages(
        uri: Uri,
        contentResolver: ContentResolver,
        scannedImageDirectory: File,
        imageQuality: ImageQuality,
        delay: Long,
        onPdfPageAdded: (Int) -> Unit
    ): List<Bitmap> = coroutineScope {
        val images = mutableListOf<Bitmap>()
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            var bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            if (bitmap.hasAlpha()) {
                // Remove alpha channel if present
                bitmap.eraseColor(-0x1)
            }
            val scaledWidth = (bitmap.width.toFloat() / bitmap.height.toFloat()) * imageQuality.getImageHeight()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth.toInt(), imageQuality.getImageHeight(), true)
            bitmap = Bitmap.createScaledBitmap(scaledBitmap, scaledWidth.toInt(), imageQuality.getImageHeight(), true)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val scannedImage = File(scannedImageDirectory, "Scanned Image ${i + 1}.jpeg")
            val outputStream = withContext(Dispatchers.IO) {
                FileOutputStream(scannedImage)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality.toBitmapQuality(), outputStream)
            withContext(Dispatchers.IO) {
                outputStream.flush()
                outputStream.close()
            }
            page.close()
            bitmap.recycle()
            onPdfPageAdded(i)
            delay(delay)
        }
        pdfRenderer.close()
        parcelFileDescriptor.close()
        images
    }

    override suspend fun saveToPdf(scannedImages: List<File>, file: File, pdfQuality: DocQuality) {
        val pdfDocument = PdfDocument()
        val fileOutputStream = withContext(Dispatchers.IO) {
            FileOutputStream(file)
        }
        for (i in scannedImages.indices) {
            val bitmap = BitmapFactory.decodeFile(scannedImages[i].absolutePath)
            val pdfSize = pdfQuality.toSizePx()
            val pdfPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pdfSize.width.toInt(), pdfSize.height.toInt(), i).create())
            val canvas = pdfPage.canvas
            val scale = (pdfSize.width / bitmap.width).coerceAtMost(pdfSize.height / bitmap.height)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            val left = (pdfSize.width - scaledBitmap.width) / 2
            val top = (pdfSize.height - scaledBitmap.height) / 2
            val paint = Paint()
            canvas.drawBitmap(scaledBitmap, left, top, paint)
            pdfDocument.finishPage(pdfPage)
            bitmap.recycle()
            delay(25)
        }
        withContext(Dispatchers.IO) {
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }

}