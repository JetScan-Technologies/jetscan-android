package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class DetectedBreak(
    @SerializedName("type")
    val type: String?
)