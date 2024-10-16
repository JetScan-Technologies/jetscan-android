package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class DetectedLanguage(
    @SerializedName("languageCode")
    val languageCode: String?,
    @SerializedName("confidence")
    val confidence: Double?
)