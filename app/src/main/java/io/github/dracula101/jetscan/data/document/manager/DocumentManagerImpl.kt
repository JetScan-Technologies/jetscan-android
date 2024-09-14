package io.github.dracula101.jetscan.data.document.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManager
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManager
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.doc.DocQuality
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.document.utils.getImageHeight
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okio.use
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
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
    private val documentDirectory: File,
    private val extensionManager: ExtensionManager,
    private val mimeTypeManager: MimeTypeManager,
    private val imageManager: ImageManager,
    private val pdfManager: PdfManager,
) : DocumentManager {

    private val contentResolver: ContentResolver = context.contentResolver
    private val scannedDirectory = File(documentDirectory, SCANNED_DOCUMENTS_FOLDER)

    private val documentFlow = MutableStateFlow(emptyList<DocumentDirectory>())
    override val localDocumentFlow: Flow<List<DocumentDirectory>> = documentFlow.asStateFlow()

    init { preCheck() }

    private fun preCheck() {
        if (!documentDirectory.exists()) {
            documentDirectory.mkdirs()
        }
        if (!scannedDirectory.exists()) {
            scannedDirectory.mkdirs()
        }
        updateFlow()
    }

    private fun updateFlow() {
        val documents = scannedDirectory.listFiles()?.map {
            val originalFile = it.listFiles()?.find { file -> file.path.contains(ORIGINAL_DOCUMENT_NAME) }
            DocumentDirectory(
                it,
                File(it, SCANNED_DOCUMENTS_IMAGES_FOLDER),
                originalFile,
                File(it, SCANNED_DOCUMENTS_PREVIEW_IMAGE)
            )
        } ?: emptyList()
        documentFlow.value = documents
    }

    override suspend fun addDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (current: Float, total: Int) -> Unit
    ): Task<DocumentDirectory> = coroutineScope {
        try {
            val extension = extensionManager.getExtensionType(contentResolver, uri)
            if (extension?.isDocument() == false) throw IllegalArgumentException("Only Pdf/Image files are supported.")
            val isPdf = extension == Extension.PDF
            val numOfPdfPages = pdfManager.getPdfPages(contentResolver, uri) ?: 0
            val directoryName = hashEncoder(fileName)
            val mainFileDirectory = File(scannedDirectory, directoryName).also { it.mkdirs() }
            val scannedImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_IMAGES_FOLDER).also { it.mkdirs() }
            if (isPdf) {
                pdfManager.loadPdfAsyncPages(
                    uri,
                    contentResolver,
                    scannedImageDirectory,
                    imageQuality = imageQuality,
                ) { progressListener.invoke(it + 1f, numOfPdfPages + 1) }
                // save the file to Original.pdf in mainFileDirectory
                val originalFile = File(mainFileDirectory, "${ORIGINAL_DOCUMENT_NAME}.pdf")
                val fileOutputStream = withContext(Dispatchers.IO) { originalFile.outputStream() }
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    withContext(Dispatchers.IO) { inputStream.copyTo(fileOutputStream) }
                }
                withContext(Dispatchers.IO) { fileOutputStream.close() }
                if (originalFile.length() == 0L) {
                    return@coroutineScope Task.Error(Exception("Error creating file"))
                }
                val previewFileDirectory = savePreviewImageUri(uri, mainFileDirectory, isPdf = true)
                progressListener.invoke(numOfPdfPages + 1f, numOfPdfPages + 1)
                if (previewFileDirectory !is Task.Success) {
                    return@coroutineScope Task.Error(Exception("Error creating preview image"))
                }
                updateFlow()
                return@coroutineScope Task.Success(
                    DocumentDirectory(
                        mainFileDirectory,
                        scannedImageDirectory,
                        originalFile,
                        previewFileDirectory.data
                    )
                )
            } else {
                val bitmap = imageManager.getBitmapFromUri(contentResolver, uri)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, imageQuality.toBitmapQuality(), outputStream)
                progressListener.invoke(0.5f, 1)
                val byteArray = outputStream.toByteArray()
                val scannedImage = File(
                    scannedImageDirectory,
                    "$SCANNED_DOCUMENT_IMAGE_PREFIX 1.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                )
                scannedImage.writeBytes(byteArray)
                if (scannedImageDirectory.parentFile == null) {
                    return@coroutineScope Task.Error(Exception("Error creating file"))
                }
                val originalFile = File(mainFileDirectory, "${ORIGINAL_DOCUMENT_NAME}.$extension")
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    originalFile.outputStream().use { output -> input.copyTo(output)}
                }
                val previewImageDirectory = savePreviewImageUri(
                    uri,
                    mainFileDirectory,
                    false
                )
                progressListener.invoke(1f, 1)
                if (previewImageDirectory !is Task.Success) {
                    return@coroutineScope Task.Error(Exception("Error creating preview image"))
                }
                updateFlow()
                return@coroutineScope Task.Success(
                    DocumentDirectory(
                        mainFileDirectory,
                        scannedImageDirectory,
                        originalFile,
                        previewImageDirectory.data
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
            Task.Error(e)
        }
    }

    override suspend fun addDocumentFromScanner(
        bitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int,
        delayDuration: Long,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): Task<DocumentDirectory> = coroutineScope {
        return@coroutineScope try {
            showInfoForBitmap(bitmaps)
            val directoryName = hashEncoder(fileName)
            val mainFileDirectory = File(scannedDirectory, directoryName).also { it.mkdirs() }
            val scannedImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_IMAGES_FOLDER).also { it.mkdirs() }
            val originalFile = File(mainFileDirectory, "${ORIGINAL_DOCUMENT_NAME}.pdf")
            val previewImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_PREVIEW_IMAGE).also { it.mkdirs() }
            var previewImageFile: File? = null
            bitmaps.forEachIndexed { index, bitmap ->
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, imageQuality, outputStream)
                val byteArray = outputStream.toByteArray()
                val file = File(
                    scannedImageDirectory,
                    "$SCANNED_DOCUMENT_IMAGE_PREFIX ${index + 1}.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                )
                file.writeBytes(byteArray)
                progressListener.invoke(index + 1f, bitmaps.size)
                if (index == 0) {
                    previewImageFile =
                        File(previewImageDirectory, "$PREVIEW_FILE_NAME.$PREVIEW_FILE_EXTENSION")
                    previewImageFile?.writeBytes(byteArray)
                }
                outputStream.close()
                delay(delayDuration)
            }
            val files = scannedImageDirectory.listFiles()
            if (files != null) {
                pdfManager.saveToPdf(files.toList(), originalFile, DocQuality.PPI_72)
            }
            if (originalFile.length() == 0L) {
                return@coroutineScope Task.Error(Exception("Error creating file"))
            }
            val scannedDocument = DocumentDirectory(
                mainFileDirectory,
                scannedImageDirectory,
                originalFile,
                previewImageFile!!
            )
            updateFlow()
            Task.Success(scannedDocument)
        } catch (e: Exception) {
            Timber.e(e)
            Task.Error(e)
        }
    }

    override fun deleteDocument(fileName: String): Boolean {
        val removedFileName = hashEncoder(fileName)
        val file = File(scannedDirectory, removedFileName)
        return if (file.exists()) {
            file.deleteRecursively()
        } else {
            false
        }
    }


    override fun getBitmapFromUri(uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            Bitmap.createBitmap(BitmapFactory.decodeStream(inputStream))
        }
    }

    private fun showInfoForBitmap(bitmaps: List<Bitmap>) {
        Timber.i("${bitmaps.size} Bitmaps")
        bitmaps.forEachIndexed { index, bitmap ->
            Timber.i("Bitmap ${index + 1}: ${bitmap.byteCount.bytesToReadableSize()}, ${bitmap.width}x${bitmap.height}")
        }
    }

    override fun savePreviewImageUri(uri: Uri, mainDirectory: File, isPdf: Boolean): Task<File> {
        val imageQuality = ImageQuality.LOW
        var image = if (isPdf) {
            pdfManager.getPdfPage(contentResolver, uri, 0)
        } else {
            imageManager.getBitmapFromUri(contentResolver, uri)
        }
        if (image == null) {
            return Task.Error(Exception("Error creating preview image"))
        }
        val outputStream = ByteArrayOutputStream()
        val scaledWidth =
            (image.width.toFloat() / image.height.toFloat()) * imageQuality.getImageHeight()
                .toFloat()
        Timber.i("Scaled Width: $scaledWidth, Image: ${image.width}x${image.height}")
        image = Bitmap.createScaledBitmap(
            image,
            scaledWidth.toInt(),
            imageQuality.getImageHeight(),
            true
        )
        image.compress(CompressFormat.PNG, imageQuality.toBitmapQuality(), outputStream)
        val byteArray = outputStream.toByteArray()
        return if (mainDirectory.isDirectory) {
            val previewDirectory =
                File(mainDirectory, SCANNED_DOCUMENTS_PREVIEW_IMAGE)
            if (!previewDirectory.exists()) {
                previewDirectory.mkdirs()
            }
            val previewFile = File(previewDirectory, "$PREVIEW_FILE_NAME.$PREVIEW_FILE_EXTENSION")
            previewFile.writeBytes(byteArray)
            Task.Success(previewFile)
        } else {
            Task.Error(Exception("Directory not found"))
        }
    }

    override fun getFileName(uri: Uri, withoutExtension: Boolean): String? {
        var result: String? = null
        if (uri.scheme.equals("content", ignoreCase = true)) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursorPosition ->
                if (cursorPosition != null && cursorPosition.moveToFirst()) {
                    cursorPosition.getColumnIndex(OpenableColumns.DISPLAY_NAME).also { index ->
                        result = cursorPosition.getString(index)
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

    override fun getFileLength(uri: Uri): Long {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }

    override fun formatFileSize(file: File): String {
        val size = file.length()
        val sizeUnit = 1024
        if (size <= 0) {
            return "0"
        }
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(sizeUnit.toDouble())).toInt()
        return DecimalFormat("#,##0.#").format(
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
            else -> "B"
        }
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

    override fun getMimeType(uri: Uri): MimeType {
        return mimeTypeManager.getMimeType(contentResolver, uri)
    }

    override fun getExtension(uri: Uri): Extension {
        return extensionManager.getExtensionType(contentResolver, uri) ?: Extension.OTHER
    }

    private fun hashEncoder(s: String): String {
        try {
            val digest = java.security.MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: java.security.NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    companion object {
        private const val SCANNED_DOCUMENTS_FOLDER = "Scanned Documents"
        private const val SCANNED_DOCUMENTS_IMAGES_FOLDER = "Images"
        private const val SCANNED_DOCUMENT_IMAGE_PREFIX = "Scanned Image"
        private const val SCANNED_DOCUMENT_IMAGE_EXTENSION = "jpeg"

        private const val ORIGINAL_DOCUMENT_NAME = "Original"

        private const val SCANNED_DOCUMENTS_PREVIEW_IMAGE = "Preview"
        private const val PREVIEW_FILE_NAME = "Preview"
        private const val PREVIEW_FILE_EXTENSION = "png"
    }

}
