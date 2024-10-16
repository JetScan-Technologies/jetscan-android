package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Dimension(
    @SerializedName("width")
    val width: Int?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("unit")
    val unit: String?
)