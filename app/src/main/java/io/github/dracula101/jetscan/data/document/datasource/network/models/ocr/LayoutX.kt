package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class LayoutX(
    @SerializedName("boundingPoly")
    val boundingPoly: BoundingPolyX?,
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("orientation")
    val orientation: String?,
    @SerializedName("textAnchor")
    val textAnchor: TextAnchorX?
)