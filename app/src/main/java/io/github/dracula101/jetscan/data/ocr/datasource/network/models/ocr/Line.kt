package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("layout")
    val layout: LayoutX?,
    @SerializedName("detectedLanguages")
    val detectedLanguages: List<DetectedLanguageX>?
)