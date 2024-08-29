package io.github.dracula101.jetscan.data.document.manager.extension

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import io.github.dracula101.jetscan.data.document.models.Extension
import java.io.File

class ExtensionManagerImpl : ExtensionManager {

    override fun getExtensionType(contextResolver: ContentResolver, uri: Uri): Extension? {
        val mimeType = contextResolver.getType(uri)
        if (mimeType != null) {
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                return Extension.getExtensionType(extension.uppercase())
            }
        }
        return null
    }

    override fun getExtensionType(file: File?): Extension {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file?.path)
        return Extension.getExtensionType(extension)
    }

}