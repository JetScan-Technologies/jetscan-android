package io.github.dracula101.jetscan.data.document.datasource.network.models


import com.google.gson.annotations.SerializedName
import io.github.dracula101.jetscan.data.document.datasource.network.models.ocr.Result

data class PdfOcrResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("result")
    val result: Result?,
    @SerializedName("status")
    val status: Int?
)