package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("detectedBreak")
    val detectedBreak: DetectedBreak?,
    @SerializedName("layout")
    val layout: LayoutXX?,
    @SerializedName("detectedLanguages")
    val detectedLanguages: List<DetectedLanguageX>?
)