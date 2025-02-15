package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class DetectedLanguageX(
    @SerializedName("confidence")
    val confidence: Int?,
    @SerializedName("languageCode")
    val languageCode: String?
)