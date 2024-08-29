package io.github.dracula101.jetscan.data.document.models.doc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class DocumentFolder (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateCreated: Long,
    val dateModified: Long? = null,
    val documentCount: Int,
    val documents: List<Document>,
    val path: String
) : Parcelable {
    companion object {
        const val ROOT_FOLDER = "/root"
    }
}