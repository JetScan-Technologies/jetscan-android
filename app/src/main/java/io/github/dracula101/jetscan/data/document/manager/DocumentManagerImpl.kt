package io.github.dracula101.jetscan.data.document.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import io.github.dracula101.jetscan.data.document.manager.apk.ApkManager
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManager
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManager
import io.github.dracula101.jetscan.data.document.manager.video.VideoManager
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.Extension
import okio.use
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow


class DocumentManagerImpl(
    private val context: Context,
    private val extensionManager: ExtensionManager,
    private val mimeTypeManager: MimeTypeManager,
    private val imageManager: ImageManager,
    private val pdfManager: PdfManager,
    private val videoManager: VideoManager,
    private val apkManager: ApkManager
) : DocumentManager {

    override fun fromUri(context: Context, uri: Uri): Document {
        val fileName = getFileName(context.contentResolver, uri)
        val mimeType = mimeTypeManager.getMimeType(context.contentResolver, uri)
        val size = getFileLength(context.contentResolver, uri)
        // Changed from lastModified to current time
        val lastModified = System.currentTimeMillis()
        val formattedDate = formatDate(lastModified)
        val extension = extensionManager.getExtensionType(context.contentResolver, uri)
        val readableSize = getReadableFileSize(size)
        val previewImageUri = when (extension) {
            Extension.JPEG, Extension.JPG, Extension.PNG, Extension.GIF, Extension.HEIC -> imageManager.saveImageFromUri(
                contentResolver = context.contentResolver,
                uri = uri,
                name = fileName ?: "",
                path = context.filesDir.path,
            )

            Extension.PDF -> pdfManager.savePdfPage(context.contentResolver, uri, fileName ?: "", filesPath = context.filesDir.path)
            Extension.MP4 -> videoManager.saveVideoFirstFrame(context, uri, fileName ?: "")
            Extension.APK -> apkManager.saveApkIcon(context, uri, fileName ?: "")
            else -> null
        }
        return Document(
            name = fileName ?: "Unknown",
            uri = uri,
            size = size,
            dateCreated = lastModified,
            mimeType = mimeType,
            extension = extension ?: Extension.OTHER,
            previewImageUri = previewImageUri?.toUri(),
        )
    }

    override fun fromFile(file: File, applicationContext: Context): Document {
        val uri = file.toUri()
        val fileName = getFileName(applicationContext.contentResolver, uri)
        val mimeType = mimeTypeManager.getMimeType(applicationContext.contentResolver, uri)
        val size = getFileLength(applicationContext.contentResolver, uri)
        val extension = extensionManager.getExtensionType(file)
        // Changed from lastModified to current time
        val lastModified = System.currentTimeMillis()
        val formattedDate = formatDate(lastModified)
        val readableSize = getReadableFileSize(size)
        val previewImageUri = when (extension) {
            Extension.JPEG, Extension.JPG, Extension.PNG, Extension.GIF, Extension.HEIC -> imageManager.saveImageFromUri(
                contentResolver = applicationContext.contentResolver,
                uri = uri,
                name = fileName ?: "",
                path = applicationContext.filesDir.path,
            )
            Extension.PDF -> pdfManager.savePdfPage(applicationContext.contentResolver, uri, fileName ?: "",filesPath = applicationContext.filesDir.path)
            Extension.MP4 -> videoManager.saveVideoFirstFrame(applicationContext, uri, fileName ?: "")
            Extension.APK -> apkManager.saveApkIcon(applicationContext, uri, fileName ?: "")
            else -> null
        }
        return Document(
            name = fileName ?: "Unknown",
            uri = uri,
            size = size,
            dateCreated = lastModified,
            mimeType = mimeType,
            extension = extension,
            previewImageUri = previewImageUri?.toUri(),
        )
    }

    private fun getDisplayName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        try {
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun getFileName(contentResolver: ContentResolver, uri: Uri, withoutExtension: Boolean): String? {
        var result: String? = null
        if (uri.scheme.equals("content", ignoreCase = true)) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).also { index ->
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return if (withoutExtension) {
            result?.substringBeforeLast(".")
        } else {
            result
        }
    }

    override fun getFileLength(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }.also {
            Timber.i("File Length: $it")
        }
    }

    override fun formatFileSize(contentResolver: ContentResolver, file: File): String {
        val size = file.length()
        val sizeUnit = 1024
        if (size <= 0) {
            return "0"
        }
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(sizeUnit.toDouble())).toInt()
        return DecimalFormat("#,##0.#").format(
            size / sizeUnit.toDouble().pow(digitGroups.toDouble())
        ) + " " + getReadableSizeUnit(contentResolver, digitGroups)
    }

    private fun getReadableSizeUnit(contentResolver: ContentResolver, digitGroups: Int): String {
        return when (digitGroups) {
            0 -> "B"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            4 -> "TB"
            5 -> "PB"
            6 -> "EB"
            7 -> "ZB"
            8 -> "YB"
            else -> {
                contentResolver.getType(Uri.fromFile(File("/"))) ?: "Unknown"
            }
        }
    }

    override fun loadPdfFirstPage(context: Context, file: File, size: Dp, density: Float): Bitmap? {
        var pdfBitmap: Bitmap? = null
        val parcelFileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        if (parcelFileDescriptor != null) {
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)
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

    override fun getReadableFileSize(length: Long): String? {
        val size = length.toDouble()
        val sizeUnit = 1000
        if (size <= 0) {
            return "0"
        }
        val digitGroups =
            (kotlin.math.log10(size) / kotlin.math.log10(sizeUnit.toDouble())).toInt()
        return DecimalFormat("#,##0.##").format(
            size / sizeUnit.toDouble().pow(digitGroups.toDouble())
        ) + " " + getReadableSizeUnit(digitGroups)
    }

    private fun getReadableSizeUnit(digitGroups: Int): String {
        return when (digitGroups) {
            0 -> "B"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            4 -> "TB"
            5 -> "PB"
            6 -> "EB"
            7 -> "ZB"
            8 -> "YB"
            else -> "Unknown"
        }
    }

    override fun formatDate(time: Long): String {
        val calendar = Calendar.getInstance()
        val date = Date(time)
        val year = calendar.get(Calendar.YEAR)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochMilli(time)
            val formatter =
                DateTimeFormatter.ofPattern("dd MMM ${if (date.year == year) "" else "yyyy"}")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } else {
            val formatter = SimpleDateFormat(
                "dd MMM ${if (date.year == year) "" else "yyyy"}",
                Locale.getDefault()
            )
            val formattedDate = formatter.format(date)
            formattedDate
        }
    }



    override fun getDocumentName(): String{
        // get in this format day, month, year hour:second AM/PM
        val sdf = SimpleDateFormat("dd, MMM, yyyy HH:mm a", Locale.getDefault())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sdf.format(Date.from(Instant.now()))
        } else {
            sdf.format(Calendar.getInstance().time)
        }
    }
}