package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Layout(
    @SerializedName("textAnchor")
    val textAnchor: TextAnchor?,
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("boundingPoly")
    val boundingPoly: BoundingPoly?,
    @SerializedName("orientation")
    val orientation: String?
)