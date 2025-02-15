package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class DetectedLanguage(
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("languageCode")
    val languageCode: String?
)