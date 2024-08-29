package io.github.dracula101.jetscan.data.document.manager.mime

import android.content.ContentResolver
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.MimeType
import java.io.File

interface MimeTypeManager {
    fun getMimeType(contentResolver: ContentResolver, uri: Uri): MimeType
    fun getMimeType(file: File?): MimeType
}