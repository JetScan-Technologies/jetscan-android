package io.github.dracula101.jetscan.data.document.manager.extension

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import io.github.dracula101.jetscan.data.document.models.Extension
import java.io.File

class ExtensionManagerImpl : ExtensionManager {

    override fun getExtensionType(contextResolver: ContentResolver, uri: Uri): Extension {
        val extension: String?
        if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(contextResolver.getType(uri))
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(uri.path?.let { File(it) }).toString())
        }
        return extension ?.let { Extension.getExtensionType(it) } ?: Extension.OTHER
    }

    override fun getExtensionType(file: File?): Extension {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file?.path)
        return Extension.getExtensionType(extension)
    }

}