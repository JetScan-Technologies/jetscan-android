package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class LayoutXX(
    @SerializedName("boundingPoly")
    val boundingPoly: BoundingPolyXX?,
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("orientation")
    val orientation: String?,
    @SerializedName("textAnchor")
    val textAnchor: TextAnchorXX?
)