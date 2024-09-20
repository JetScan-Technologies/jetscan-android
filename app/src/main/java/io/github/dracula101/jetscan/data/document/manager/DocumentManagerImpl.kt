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
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.Size
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManager
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.document.utils.getImageHeight
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.pdf.manager.PdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val extraDocumentDirectory = File(documentDirectory, EXTRA_DOCUMENTS_FOLDER)

    private val documentFlow = MutableStateFlow(emptyList<DocumentDirectory>())
    override val localDocumentFlow: Flow<List<DocumentDirectory>> = documentFlow.asStateFlow()

    init { preCheck() }

    private fun preCheck() {
        if (!documentDirectory.exists()) {
            documentDirectory.mkdirs()
        }
        if (!extraDocumentDirectory.exists()) {
            extraDocumentDirectory.mkdirs()
        }
        if (!scannedDirectory.exists()) {
            scannedDirectory.mkdirs()
        }
        updateFlow()
    }

    private fun updateFlow() {
        val documents = scannedDirectory.listFiles()?.map { mainDir ->
            val imageDir = File(mainDir, SCANNED_DOCUMENTS_ORIGINAL_FOLDER)
            val scannedImageDir = File(mainDir, SCANNED_DOCUMENTS_SCANNED_IMAGES_FOLDER)
            val originalFile = mainDir.listFiles()?.find { file -> (file.isFile && file.path.contains(".pdf")) }
            val previewFile = File(mainDir, SCANNED_DOCUMENTS_SCANNED_IMAGES_FOLDER).listFiles()?.firstOrNull() ?: File(mainDir, SCANNED_DOCUMENTS_ORIGINAL_FOLDER).listFiles()?.firstOrNull()
            if (originalFile == null) { return@map null }
            DocumentDirectory(
                mainDir = mainDir,
                imageDir = imageDir,
                scannedImageDir = scannedImageDir,
                originalFile = originalFile,
                previewFile = previewFile,
            )
        } ?: emptyList()
        documentFlow.value = documents.filterNotNull()
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
            // Create main directory
            val mainFileDirectory = File(scannedDirectory, directoryName).also { it.mkdirs() }
            // Create original image directory (without cropping enabled as import will not auto crop documents)
            val originalImageDirectory = File(mainFileDirectory, SCANNED_DOCUMENTS_ORIGINAL_FOLDER).also { it.mkdirs() }
            // Create scanned Image Directory, (if the file has been applied some filters)
            val scannedImageDir = File(mainFileDirectory, SCANNED_DOCUMENTS_SCANNED_IMAGES_FOLDER).also { it.mkdirs() }
            if (isPdf) {
                progressListener.invoke(0f, numOfPdfPages + 1)
                // Saving files into the original image directory
                pdfManager.loadPdfAsyncPages(
                    uri,
                    contentResolver,
                    originalImageDirectory,
                    SCANNED_DOCUMENT_IMAGE_PREFIX,
                    imageQuality = imageQuality.toBitmapQuality(),
                    resizedHeight = imageQuality.getImageHeight(),
                    fileExtension = SCANNED_DOCUMENT_IMAGE_EXTENSION
                ) { progressListener.invoke(it + 1f, numOfPdfPages + 1) }
                val reformedFileName = fileName.replace(".pdf", "")
                // Saving the original pdf file
                val originalFile = File(mainFileDirectory, "$reformedFileName.pdf")
                withContext(Dispatchers.IO) {
                    val fileOutputStream = originalFile.outputStream()
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.copyTo(fileOutputStream)
                    }
                    fileOutputStream.close()
                }.runCatching {  }
                if (originalFile.length() == 0L) {
                    return@coroutineScope Task.Error(Exception("Error creating file"))
                }
                // Saving the preview image
                progressListener.invoke(numOfPdfPages + 1f, numOfPdfPages + 1)
                updateFlow()
                return@coroutineScope Task.Success(
                    DocumentDirectory(
                        mainDir = mainFileDirectory,
                        imageDir = originalImageDirectory,
                        scannedImageDir = scannedImageDir,
                        originalFile = originalFile,
                        previewFile = originalImageDirectory.listFiles()?.firstOrNull()
                    )
                )
            } else {
                progressListener.invoke(0f, 1)
                val bitmap = imageManager.getBitmapFromUri(contentResolver, uri)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, imageQuality.toBitmapQuality(), outputStream)
                progressListener.invoke(0.2f, 1)
                val byteArray = outputStream.toByteArray()
                val scannedImage = File(
                    originalImageDirectory,
                    "${SCANNED_DOCUMENT_IMAGE_PREFIX}_1.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                )
                scannedImage.writeBytes(byteArray)
                progressListener.invoke(0.5f, 1)
                if (originalImageDirectory.parentFile == null) {
                    return@coroutineScope Task.Error(Exception("Error creating file"))
                }
                // saving the original file
                val originalFile = withContext(Dispatchers.IO) {
                    val reformedFileName = fileName.replace(".jpeg", "")
                    val originalFile = File(mainFileDirectory, "$reformedFileName.$extension")
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.use { input ->
                        originalFile.outputStream().use { output -> input.copyTo(output)}
                    }
                    inputStream?.close()
                    originalFile
                }
                progressListener.invoke(1f, 1)
                updateFlow()
                return@coroutineScope Task.Success(
                    DocumentDirectory(
                        mainDir = mainFileDirectory,
                        imageDir = originalImageDirectory,
                        scannedImageDir = scannedImageDir,
                        originalFile = originalFile,
                        previewFile = originalImageDirectory.listFiles()?.firstOrNull()
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
            Task.Error(e)
        }
    }

    override suspend fun addDocumentFromScanner(
        originalBitmaps: List<Bitmap>,
        scannedBitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int,
        delayDuration: Long,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): Task<DocumentDirectory> = coroutineScope {
        return@coroutineScope try {
            showInfoForBitmap(originalBitmaps)
            val directoryName = hashEncoder(fileName)
            // Create main directory
            val mainFileDirectory = File(scannedDirectory, directoryName).also { it.mkdirs() }
            // Create original image directory (without cropping - this is to edit the original document later)
            val originalImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_ORIGINAL_FOLDER).also { it.mkdirs() }
            // Create scanned Image Directory, (if the file has been applied cropping and filters)
            val scannedImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_SCANNED_IMAGES_FOLDER).also { it.mkdirs() }
            // Saving the original images
            val reformedFileName = fileName.replace(".pdf", "")
            val originalFile = File(mainFileDirectory, "$reformedFileName.pdf")
            progressListener.invoke(1f, originalBitmaps.size * 2)
            var currentProgress = 1f
            List(originalBitmaps.size) { index ->
                async(Dispatchers.IO) {
                    val originalImage = File(
                        originalImageDirectory,
                        "${SCANNED_DOCUMENT_IMAGE_PREFIX}_${index + 1}.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                    )
                    originalImage.outputStream().use {
                        originalBitmaps[index].compress(CompressFormat.JPEG,imageQuality,it)
                    }
                    progressListener.invoke(currentProgress++, originalBitmaps.size * 2)
                    val scannedImage = File(
                        scannedImageDirectory,
                        "${SCANNED_DOCUMENT_SCANNED_IMAGE_PREFIX}_${index + 1}.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                    )
                    scannedImage.outputStream().use {
                        scannedBitmaps[index].compress(CompressFormat.JPEG,imageQuality,it)
                    }
                    progressListener.invoke(currentProgress++, originalBitmaps.size * 2)
                }
            }.awaitAll()
            val files = scannedImageDirectory.listFiles()
            if (files != null) {
                pdfManager.savePdf(files.toList(), originalFile, imageQuality, Size(595,842), 0f)
            }
            if (originalFile.length() == 0L) {
                return@coroutineScope Task.Error(Exception("Error creating file"))
            }
            val scannedDocument = DocumentDirectory(
                mainDir = mainFileDirectory,
                imageDir = originalImageDirectory,
                scannedImageDir = scannedImageDirectory,
                originalFile = originalFile,
                previewFile = File(scannedImageDirectory, files?.first()?.name ?: "")
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

    override suspend fun addExtraDocument(file: File, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val extraDocumentFile = File(extraDocumentDirectory, fileName)
                file.copyTo(extraDocumentFile, overwrite = true)
                updateFlow()
                extraDocumentFile
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    }

    override suspend fun deleteExtraDocument(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(uri.path ?: throw IllegalArgumentException("Invalid file path"))
                if (file.exists()) {
                    file.deleteRecursively()
                    updateFlow()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e)
                false
            }
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

    private fun base64Encoder(s: String): String {
        return encodeToString(s.toByteArray(), Base64.DEFAULT)
    }

    private fun base64Decoder(s: String): String {
        return String(Base64.decode(s, Base64.DEFAULT))
    }

    companion object {
        private const val SCANNED_DOCUMENTS_FOLDER = "Scanned Documents"
        private const val SCANNED_DOCUMENTS_ORIGINAL_FOLDER = "Original Images"
        private const val SCANNED_DOCUMENTS_SCANNED_IMAGES_FOLDER = "Scanned Images"

        private const val SCANNED_DOCUMENT_IMAGE_PREFIX = "Image"
        private const val SCANNED_DOCUMENT_SCANNED_IMAGE_PREFIX = "Scanned Image"
        private const val SCANNED_DOCUMENT_IMAGE_EXTENSION = "jpg"

        private const val EXTRA_DOCUMENTS_FOLDER = "Extra Documents"

    }

}
