package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("content")
    val content: String?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("mimeType")
    val mimeType: String?,
    @SerializedName("width")
    val width: Int?
)