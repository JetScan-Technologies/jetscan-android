package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class HumanReviewStatus(
    @SerializedName("state")
    val state: String?,
    @SerializedName("stateMessage")
    val stateMessage: String?
)