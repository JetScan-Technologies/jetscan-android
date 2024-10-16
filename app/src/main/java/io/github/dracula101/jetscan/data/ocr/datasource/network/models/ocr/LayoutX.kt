package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class LayoutX(
    @SerializedName("textAnchor")
    val textAnchor: TextAnchorX?,
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("boundingPoly")
    val boundingPoly: BoundingPolyX?,
    @SerializedName("orientation")
    val orientation: String?
)