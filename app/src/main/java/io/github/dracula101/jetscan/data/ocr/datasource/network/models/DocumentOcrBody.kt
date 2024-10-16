package io.github.dracula101.jetscan.data.ocr.datasource.network.models

import com.google.gson.annotations.SerializedName

data class DocumentOcrBody(
    @SerializedName("rawDocument")
    val document: RawDocument,
)

data class RawDocument(
    @SerializedName("content")
    val content: String,
    @SerializedName("mimeType")
    val mimeType: String,
)