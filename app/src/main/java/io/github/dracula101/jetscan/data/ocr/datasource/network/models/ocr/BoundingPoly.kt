package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class BoundingPoly(
    @SerializedName("vertices")
    val vertices: List<Vertice?>?,
    @SerializedName("normalizedVertices")
    val normalizedVertices: List<NormalizedVertice?>?
)