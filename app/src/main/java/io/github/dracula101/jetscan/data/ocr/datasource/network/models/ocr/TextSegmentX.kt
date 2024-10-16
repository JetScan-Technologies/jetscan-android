package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class TextSegmentX(
    @SerializedName("endIndex")
    val endIndex: String?,
    @SerializedName("startIndex")
    val startIndex: String?
)