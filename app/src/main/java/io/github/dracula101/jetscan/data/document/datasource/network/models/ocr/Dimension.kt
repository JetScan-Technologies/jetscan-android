package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Dimension(
    @SerializedName("height")
    val height: Int?,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("width")
    val width: Int?
)