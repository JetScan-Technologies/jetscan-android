package io.github.dracula101.jetscan.data.platform.manager.file_action

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.github.dracula101.jetscan.presentation.features.document.pdfview.components.PdfDocumentAdapter
import java.io.File
import java.util.Locale


class FileActionManagerImpl(
    private val activity: Activity
) : FileActionManager {

    private fun getMimeType(uri: Uri): String? {
        val mimeType: String?
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = activity.contentResolver
            mimeType = cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }

    override fun saveFileIntent(
        uri: Uri,
        title: String,
    ): Intent {
        val mimeType = getMimeType(uri)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, title)
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    @Composable
    override fun saveFileWithLauncher(
        uri: () -> Uri
    ): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val intentUri = result.data?.data ?: return@rememberLauncherForActivityResult
            val file = FileProvider.getUriForFile(
                activity,
                activity.packageName + ".provider",
                File(uri.invoke().path!!)
            )
            if (result.resultCode == Activity.RESULT_OK) {
                val contentResolver = activity.contentResolver
                contentResolver.openOutputStream(intentUri)?.use { outputStream ->
                    contentResolver.openInputStream(file)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    override fun shareFile(
        uri: Uri,
        title: String,
        subject: String,
        onActivityNotFound: () -> Unit
    ) {
        val mimeType = getMimeType(uri)
        val intent = Intent(Intent.ACTION_SEND).also {
            it.type = mimeType
            it.putExtra(Intent.EXTRA_STREAM, uri)
            it.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            activity.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            onActivityNotFound()
        }
    }

    override fun shareToApp(
        uri: Uri,
        packageName: String,
        title: String,
        subject: String,
        onActivityNotFound: () -> Unit
    ) {
        val mimeType = getMimeType(uri)
        val intent = Intent(Intent.ACTION_SEND).also {
            it.setPackage(packageName)
            it.setDataAndType(uri, mimeType)
            it.putExtra(Intent.EXTRA_STREAM, uri)
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            it.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            activity.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            onActivityNotFound()
        }
    }

    override fun shareToGDrive(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    ) {
        shareToApp(
            uri = uri,
            packageName = "com.google.android.apps.docs",
            subject = subject,
            title = "Share to Google Drive",
            onActivityNotFound = activityNotFound
        )
    }

    override fun shareToWhatsapp(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    ) {
        shareToApp(
            uri = uri,
            packageName = "com.whatsapp",
            subject = subject,
            title = "Share to WhatsApp",
            onActivityNotFound = activityNotFound
        )
    }

    override fun shareToEmail(
        uri: Uri,
        subject: String,
        activityNotFound: () -> Unit
    ) {
        shareToApp(
            uri = uri,
            packageName = "com.google.android.gm",
            subject = subject,
            title = "Share to Email",
            onActivityNotFound = activityNotFound
        )
    }

    override fun shareToPrinter(
        file: File,
        subject: String,
        activityNotFound: () -> Unit
    ) {
        val printIntent = activity.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
        val printAdapter = PdfDocumentAdapter(file)
        printIntent.print(subject, printAdapter, PrintAttributes.Builder().build())
    }

}