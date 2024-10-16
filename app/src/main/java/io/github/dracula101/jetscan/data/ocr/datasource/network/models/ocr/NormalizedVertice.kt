package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class NormalizedVertice(
    @SerializedName("x")
    val x: Int?,
    @SerializedName("y")
    val y: Int?
)