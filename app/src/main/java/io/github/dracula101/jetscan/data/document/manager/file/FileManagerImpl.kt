package io.github.dracula101.jetscan.data.document.manager.file

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Parcelable
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManager
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.Task
import io.github.dracula101.jetscan.data.document.utils.getImageHeight
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import io.github.dracula101.jetscan.data.platform.utils.DateFormatter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File


class FileManagerImpl (
    private val pdfManager: PdfManager,
    private val imageManager: ImageManager,
    private val documentManager: DocumentManager,
    private val extensionManager: ExtensionManager,
    private val fileManagerDirectory: File,
    private val contentResolver: ContentResolver
) : FileManager {

    private val importDirectory = File(fileManagerDirectory, IMPORTED_DOCUMENTS_FOLDER)
    private val scannedDirectory = File(fileManagerDirectory, SCANNED_DOCUMENTS_FOLDER)

    init {
        if (!fileManagerDirectory.exists()) {
            fileManagerDirectory.mkdirs()
        }
        if (!scannedDirectory.exists()) {
            scannedDirectory.mkdirs()
        }
        preCheck()
        Timber.i("Files: ")
        Timber.i(
            "${
                importDirectory.listFiles()?.map {
                    "File: ${it.name} - ${it.length()} bytes\n"
                }
            }"
        )
    }

    private fun preCheck() {
        if (!importDirectory.exists()) {
            importDirectory.mkdirs()
        }
        if (!scannedDirectory.exists()) {
            scannedDirectory.mkdirs()
        }
    }

    override fun getImportDocuments(): List<File> {
        preCheck()
        return importDirectory.listFiles()?.toList() ?: emptyList()
    }

    override fun getImportDocument(name: String): File? {
        preCheck()
        val files = importDirectory.listFiles()
        for (file in files ?: emptyArray()) {
            if (file.name == name) {
                return file
            }
        }
        return null
    }

    override fun uploadImportDocument(file: File): Boolean {
        preCheck()
        return try {
            Timber.i("Uploading file: $file")
            val newFile = File(importDirectory, file.name)
            file.copyTo(newFile, true)
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override fun uploadImportDocumentFile(file: File): File? {
        preCheck()
        return try {
            Timber.i("Uploading file: $file")
            val newFile = File(importDirectory, file.name)
            file.copyTo(newFile, true)
            newFile
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun uploadImportDocument(document: Document): Boolean {
        preCheck()
        return try {
            val file = File(importDirectory, document.name)
            val inputStream = contentResolver.openInputStream(document.uri).use {
                it?.readBytes()
            }
            file.writeBytes(inputStream ?: byteArrayOf())
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override fun deleteImportDocument(file: File) {
        preCheck()
        val files = importDirectory.listFiles()
        val fileToDelete = files?.find { it.name == file.name }
        fileToDelete?.delete()
    }

    override fun deleteImportDocuments() {
        preCheck()
        importDirectory.listFiles()?.forEach { it.delete() }
    }

    override suspend fun addScannedDocument(
        uri: Uri,
        imageQuality: ImageQuality,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit
    ): Task<ScannedDocDirectory> = coroutineScope {
        return@coroutineScope try {
            Timber.i("Adding Scanned Document: $uri")
            val extension = extensionManager.getExtensionType(contentResolver, uri)
            if (extension?.isDocument() == false)
                throw IllegalArgumentException("Only Pdf/Image files are supported.")
            val isPdf = extension == Extension.PDF
            val fileName = documentManager.getFileName(contentResolver, uri)
            val numOfPdfPages = pdfManager.getPdfPages(contentResolver, uri) ?: 0
            Timber.i("Num of Pages: $numOfPdfPages", "File Name: $fileName")
            val directoryName = hashEncoder(fileName ?: "JetScan Document ${DateFormatter.formatCurrentDate()}")
            val mainFileDirectory = File(scannedDirectory, directoryName)
            if (!mainFileDirectory.exists()) {
                mainFileDirectory.mkdirs()
            }
            val scannedImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_IMAGES_FOLDER)
            if (!scannedImageDirectory.exists()) {
                scannedImageDirectory.mkdirs()
            }
            if (isPdf) {
                pdfManager.loadPdfAsyncPages(
                    uri,
                    contentResolver,
                    scannedImageDirectory,
                    imageQuality = imageQuality,
                ) {
                    progressListener.invoke(it + 1f, numOfPdfPages + 1)
                }
                val previewFileDirectory =
                    savePreviewImageUri(uri, mainFileDirectory, isPdf = true)
                progressListener.invoke(numOfPdfPages + 1f, numOfPdfPages + 1)
                if (previewFileDirectory is Task.Success) {
                    return@coroutineScope Task.Success(
                        ScannedDocDirectory(
                            mainFileDirectory,
                            scannedImageDirectory,
                            previewFileDirectory.data
                        )
                    )
                } else {
                    return@coroutineScope Task.Error(Exception("Error creating preview image"))
                }
            } else {
                val bitmap = imageManager.getBitmapFromUri(contentResolver, uri)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(
                    CompressFormat.JPEG,
                    imageQuality.toBitmapQuality(),
                    outputStream
                )
                progressListener.invoke(0.5f, 1)
                val byteArray = outputStream.toByteArray()
                val scannedImage = File(scannedImageDirectory, "Scanned Image.jpeg")
                scannedImage.writeBytes(byteArray)
                if (scannedImageDirectory.parentFile != null) {
                    val previewImageDirectory = savePreviewImageUri(
                        uri,
                        mainFileDirectory,
                        false
                    )
                    progressListener.invoke(1f, 1)
                    if (previewImageDirectory is Task.Success) {
                        return@coroutineScope Task.Success(
                            ScannedDocDirectory(
                                mainFileDirectory,
                                scannedImageDirectory.parentFile!!,
                                previewImageDirectory.data
                            )
                        )
                    } else {
                        return@coroutineScope Task.Error(Exception("Error creating preview image"))
                    }
                } else {
                    return@coroutineScope Task.Error(Exception("Error creating file"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            Task.Error(e)
        }
    }

    override suspend fun addScannedDocumentFromScanner(
        bitmaps: List<Bitmap>,
        fileName: String,
        imageQuality: Int,
        delayDuration: Long,
        progressListener: (currentProgress: Float, totalProgress: Int) -> Unit,
    ): Task<ScannedDocDirectory> = coroutineScope {
        return@coroutineScope try {
            showInfoForBitmap(bitmaps)
            val directoryName = hashEncoder(fileName)
            val mainFileDirectory = File(scannedDirectory, directoryName).also {
                it.mkdirs()
            }
            val scannedImageDirectory =
                File(mainFileDirectory, SCANNED_DOCUMENTS_IMAGES_FOLDER).also {
                    it.mkdirs()
                }
            val previewImageDirectory = File(mainFileDirectory, SCANNED_DOCUMENTS_PREVIEW_IMAGE).also{
                it.mkdirs()
            }
            var previewImageFile : File? = null
            bitmaps.forEachIndexed { index, bitmap ->
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, imageQuality, outputStream)
                val byteArray = outputStream.toByteArray()
                val file = File(scannedImageDirectory, "Scanned Image ${index + 1}.jpeg")
                file.writeBytes(byteArray)
                progressListener.invoke(index + 1f, bitmaps.size)
                if (index == 0) {
                    previewImageFile = File(previewImageDirectory, "Preview.jpeg")
                    previewImageFile?.writeBytes(byteArray)
                }
                outputStream.close()
                delay(delayDuration)
            }
            val scannedDocument = ScannedDocDirectory(
                mainFileDirectory,
                scannedImageDirectory,
                previewImageFile!!
            )
            Task.Success(scannedDocument)
        } catch (e: Exception) {
            Timber.e(e)
            Task.Error(e)
        }
    }

    private fun showInfoForBitmap(bitmaps: List<Bitmap>) {
        Timber.i("${bitmaps.size} Bitmaps")
        bitmaps.forEachIndexed { index, bitmap ->
            Timber.i("Bitmap ${index + 1}: ${getReadableSizeForPdf(bitmap.byteCount)}, ${bitmap.width}x${bitmap.height}")
        }
    }

    private fun getReadableSizeForPdf(bytes: Int): String {
        // bytes, kb, mb, gb
        val kb = 1000
        val mb = kb * 1000
        val gb = mb * 1000
        return when {
            bytes < kb -> "$bytes bytes"
            bytes < mb -> "${bytes / kb} KB"
            bytes < gb -> "${bytes / mb} MB"
            else -> "${bytes / gb} GB"
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
            val previewFile = File(previewDirectory, "Preview.png")
            previewFile.writeBytes(byteArray)
            Timber.i("Preview Image: ${image.width}x${image.height}")
            Task.Success(previewFile)
        } else {
            Task.Error(Exception("Directory not found"))
        }
    }

    override fun deleteScannedDocument(fileName: String): Boolean {
        val removedFileName = hashEncoder(fileName)
        val file = File(scannedDirectory, removedFileName)
        return if (file.exists()) {
            file.deleteRecursively()
        } else {
            false
        }
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
        private const val IMPORTED_DOCUMENTS_FOLDER = "Imported Documents"
        private const val SCANNED_DOCUMENTS_FOLDER = "Scanned Documents"
        private const val SCANNED_DOCUMENTS_IMAGES_FOLDER = "Images"
        private const val SCANNED_DOCUMENTS_PREVIEW_IMAGE = "Preview"
    }
}


@Parcelize
data class ScannedDocDirectory(
    val mainDirectory: File,
    val scannedImageDirectory: File,
    val previewImage: File
) : Parcelable {
    @Override
    override fun toString(): String {
        return "Main Directory: ${mainDirectory.path}, Scanned Images: ${scannedImageDirectory.listFiles()}, Preview Image: ${previewImage.path}"
    }
}