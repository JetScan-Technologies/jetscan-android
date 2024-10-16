package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class TextSegment(
    @SerializedName("endIndex")
    val endIndex: String?
)