package io.github.dracula101.jetscan.data.document.manager.extension

import android.content.ContentResolver
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.Extension
import java.io.File

interface ExtensionManager {

    fun getExtensionType(contextResolver: ContentResolver, uri: Uri): Extension

    fun getExtensionType(file: File?): Extension

}