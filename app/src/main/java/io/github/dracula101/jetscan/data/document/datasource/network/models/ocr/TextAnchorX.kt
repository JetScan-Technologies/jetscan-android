package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class TextAnchorX(
    @SerializedName("textSegments")
    val textSegments: List<TextSegmentX>?
)