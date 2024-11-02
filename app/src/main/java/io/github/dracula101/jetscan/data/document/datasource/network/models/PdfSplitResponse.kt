package io.github.dracula101.jetscan.data.document.datasource.network.models


import com.google.gson.annotations.SerializedName

data class PdfSplitResponse(
    @SerializedName("files")
    val files: List<String?>?,
    @SerializedName("paths")
    val paths: List<String?>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Int?
)