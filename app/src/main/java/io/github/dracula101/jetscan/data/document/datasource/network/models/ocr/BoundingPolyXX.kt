package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class BoundingPolyXX(
    @SerializedName("normalizedVertices")
    val normalizedVertices: List<NormalizedVertice>?,
    @SerializedName("vertices")
    val vertices: List<Vertice>?
)