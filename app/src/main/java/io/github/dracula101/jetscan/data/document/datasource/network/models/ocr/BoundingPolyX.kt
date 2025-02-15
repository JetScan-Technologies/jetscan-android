package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class BoundingPolyX(
    @SerializedName("normalizedVertices")
    val normalizedVertices: List<NormalizedVerticeX>?,
    @SerializedName("vertices")
    val vertices: List<Vertice>?
)