package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("mimeType")
    val mimeType: String?,
    @SerializedName("pages")
    val pages: List<Page>?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("uri")
    val uri: String?
)