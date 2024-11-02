package io.github.dracula101.jetscan.data.document.datasource.network.models


import com.google.gson.annotations.SerializedName

data class PdfMergeResponse(
    @SerializedName("file")
    val file: String?,
    @SerializedName("path")
    val path: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Int?
)