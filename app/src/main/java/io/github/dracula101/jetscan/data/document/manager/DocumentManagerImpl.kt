package io.github.dracula101.jetscan.data.document.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.Size
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManager
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerErrorType
import io.github.dracula101.jetscan.data.document.manager.models.DocManagerResult
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.getImageHeight
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import io.github.dracula101.jetscan.data.platform.utils.bytesToReadableSize
import io.github.dracula101.pdf.manager.PdfManager
import io.github.dracula101.pdf.models.PdfOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okio.use
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

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
    }

    override suspend fun addDocument(
        uri: Uri,
        fileName: String,
        imageQuality: ImageQuality,
        progressListener: (current: Float, total: Int) -> Unit
    ): DocManagerResult<DocumentDirectory> = coroutineScope {
        try {
            val extension = extensionManager.getExtensionType(contentResolver, uri)
            if (!extension.isDocument()) {
                return@coroutineScope DocManagerResult.Error(
                    message = "Invalid file type - ${extension.name}",
                    type = DocManagerErrorType.INVALID_EXTENSION
                )
            }
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
                    return@coroutineScope DocManagerResult.Error(
                        message = "Error creating file",
                        type = DocManagerErrorType.FILE_NOT_CREATED
                    )
                }
                // Saving the preview image
                progressListener.invoke(numOfPdfPages + 1f, numOfPdfPages + 1)
                return@coroutineScope DocManagerResult.Success(
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
                    return@coroutineScope DocManagerResult.Error(
                        message = "Error creating file",
                        type = DocManagerErrorType.FILE_NOT_CREATED
                    )
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
                return@coroutineScope DocManagerResult.Success(
                    DocumentDirectory(
                        mainDir = mainFileDirectory,
                        imageDir = originalImageDirectory,
                        scannedImageDir = scannedImageDir,
                        originalFile = originalFile,
                        previewFile = originalImageDirectory.listFiles()?.firstOrNull()
                    )
                )
            }
        } catch (error: Exception) {
            Timber.e(error)
            DocManagerResult.Error(
                message = when(error){
                    is IOException -> "Error creating file"
                    else -> "Error adding document"
                },
                error = error,
                type = when(error){
                    is IOException -> DocManagerErrorType.IO_EXCEPTION
                    else -> DocManagerErrorType.UNKNOWN
                }
            )
        }
    }

    override suspend fun addDocumentFromScanner(
        originalBitmaps: List<Bitmap>,
        scannedBitmaps: List<Bitmap>,
        fileName: String,
        delayDuration: Long,
        pdfOptions: PdfOptions,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): DocManagerResult<DocumentDirectory> = coroutineScope {
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
                        originalBitmaps[index].compress(CompressFormat.JPEG,pdfOptions.imageQuality,it)
                    }
                    progressListener.invoke(currentProgress++, originalBitmaps.size * 2)
                    val scannedImage = File(
                        scannedImageDirectory,
                        "${SCANNED_DOCUMENT_SCANNED_IMAGE_PREFIX}_${index + 1}.$SCANNED_DOCUMENT_IMAGE_EXTENSION"
                    )
                    scannedImage.outputStream().use {
                        scannedBitmaps[index].compress(CompressFormat.JPEG,pdfOptions.imageQuality,it)
                    }
                    progressListener.invoke(currentProgress++, originalBitmaps.size * 2)
                }
            }.awaitAll()
            val files = scannedImageDirectory.listFiles()
            if (files != null) {
                val isSaved = pdfManager.savePdf(
                    files = files.toList(),
                    output = originalFile,
                    options = pdfOptions
                )
                if (!isSaved) {
                    return@coroutineScope DocManagerResult.Error(
                        message = "Error creating file",
                        type = DocManagerErrorType.FILE_NOT_CREATED
                    )
                }
            }
            if (originalFile.length() == 0L) {
                return@coroutineScope DocManagerResult.Error(
                    message = "Error creating file",
                    type = DocManagerErrorType.FILE_NOT_CREATED
                )
            }
            val scannedDocument = DocumentDirectory(
                mainDir = mainFileDirectory,
                imageDir = originalImageDirectory,
                scannedImageDir = scannedImageDirectory,
                originalFile = originalFile,
                previewFile = File(scannedImageDirectory, files?.first()?.name ?: "")
            )
            DocManagerResult.Success(scannedDocument)
        } catch (error: Exception) {
            Timber.e(error)
            DocManagerResult.Error(
                message = when(error){
                    is IOException -> "Error creating document"
                    else -> "Error adding document"
                },
                error = error,
                type = when(error){
                    is IOException -> DocManagerErrorType.IO_EXCEPTION
                    else -> DocManagerErrorType.UNKNOWN
                }
            )
        }
    }

    override fun deleteDocument(fileName: String): DocManagerResult<Boolean> {
        val removedFileName = hashEncoder(fileName)
        val file = File(scannedDirectory, removedFileName)
        return if (file.exists()) {
            file.deleteRecursively()
            DocManagerResult.Success(true)
        } else {
            DocManagerResult.Success(false)
        }
    }

    override suspend fun addExtraDocument(file: File, fileName: String): DocManagerResult<File?> {
        return withContext(Dispatchers.IO) {
            try {
                val extraDocumentFile = File(extraDocumentDirectory, fileName)
                if (extraDocumentFile.exists()) {
                    extraDocumentFile.delete()
                }
                file.copyTo(extraDocumentFile)
                DocManagerResult.Success(extraDocumentFile)
            } catch (e: Exception) {
                Timber.e(e)
                DocManagerResult.Error(
                    message = "Error adding extra document",
                    error = e,
                    type = DocManagerErrorType.FILE_NOT_CREATED
                )
            }
        }
    }

    override suspend fun deleteExtraDocument(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(uri.path ?: throw IllegalArgumentException("Invalid file path"))
                if (file.exists()) {
                    file.deleteRecursively()
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

    override fun getMimeType(uri: Uri): MimeType {
        return mimeTypeManager.getMimeType(contentResolver, uri)
    }

    override fun getExtension(uri: Uri): Extension {
        return extensionManager.getExtensionType(contentResolver, uri)
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
