package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("layout")
    val layout: LayoutX?,
    @SerializedName("detectedBreak")
    val detectedBreak: DetectedBreak?,
    @SerializedName("detectedLanguages")
    val detectedLanguages: List<DetectedLanguageX>?
)