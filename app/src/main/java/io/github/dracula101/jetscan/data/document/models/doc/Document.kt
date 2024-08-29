package io.github.dracula101.jetscan.data.document.models.doc


import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Stable
import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.document.models.image.ScannedImage
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.UUID
import javax.annotation.concurrent.Immutable

@Immutable
@Stable
@Parcelize
data class Document(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri,
    val size: Long,
    val dateCreated: Long,
    val dateModified: Long? = null,
    val mimeType: MimeType? = null,
    val extension: Extension = Extension.OTHER,
    val previewImageUri: Uri? = null,
    val scannedImages: List<ScannedImage> = emptyList(),
) : Parcelable
