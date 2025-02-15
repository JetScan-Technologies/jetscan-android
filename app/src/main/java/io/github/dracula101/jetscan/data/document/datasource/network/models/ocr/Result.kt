package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("document")
    val document: Document?,
    @SerializedName("humanReviewStatus")
    val humanReviewStatus: HumanReviewStatus?
)