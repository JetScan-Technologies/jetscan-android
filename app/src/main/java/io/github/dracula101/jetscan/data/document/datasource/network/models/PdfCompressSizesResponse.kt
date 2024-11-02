package io.github.dracula101.jetscan.data.document.datasource.network.models


import com.google.gson.annotations.SerializedName

data class PdfCompressSizesResponse(
    @SerializedName("compress_sizes")
    val compressSizes: Map<String, Int>?,
    @SerializedName("file")
    val `file`: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Int?
)