package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class NormalizedVertice(
    @SerializedName("x")
    val x: Double?,
    @SerializedName("y")
    val y: Double?
)