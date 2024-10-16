package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class OcrData(
    @SerializedName("uri")
    val uri: String?,
    @SerializedName("mimeType")
    val mimeType: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("pages")
    val pages: List<Page>?
)