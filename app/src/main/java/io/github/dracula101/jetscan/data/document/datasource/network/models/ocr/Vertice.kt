package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Vertice(
    @SerializedName("x")
    val x: Int?,
    @SerializedName("y")
    val y: Int?
)