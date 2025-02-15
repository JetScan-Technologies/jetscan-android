package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class TextSegment(
    @SerializedName("endIndex")
    val endIndex: String?,
    @SerializedName("startIndex")
    val startIndex: String?
)