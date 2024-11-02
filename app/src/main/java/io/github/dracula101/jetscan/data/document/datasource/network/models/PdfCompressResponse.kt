package io.github.dracula101.jetscan.data.document.datasource.network.models


import com.google.gson.annotations.SerializedName

data class PdfCompressResponse(
    @SerializedName("file")
    val `file`: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("path")
    val path: String?,
    @SerializedName("status")
    val status: Int?
)