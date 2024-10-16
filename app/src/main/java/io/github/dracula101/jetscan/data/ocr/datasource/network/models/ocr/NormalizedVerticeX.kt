package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class NormalizedVerticeX(
    @SerializedName("x")
    val x: Double?,
    @SerializedName("y")
    val y: Double?
)