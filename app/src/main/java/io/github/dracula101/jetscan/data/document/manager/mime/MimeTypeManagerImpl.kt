package io.github.dracula101.jetscan.data.document.manager.mime

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import io.github.dracula101.jetscan.data.document.models.MimeType
import java.io.File

class MimeTypeManagerImpl : MimeTypeManager {
    override fun getMimeType(contentResolver: ContentResolver, uri: Uri): MimeType {
        val rawMimeType = when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                val cr: ContentResolver = contentResolver
                cr.getType(uri)
            }

            ContentResolver.SCHEME_FILE -> {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                    uri.toString()
                )
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.lowercase()
                )
            }

            else -> null
        }
        return MimeType.getMimeType(rawMimeType)
    }

    override fun getMimeType(file: File?): MimeType {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file?.path)
        return MimeType.getMimeType(extension)
    }
}