package io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Page(
    @SerializedName("pageNumber")
    val pageNumber: Int?,
    @SerializedName("dimension")
    val dimension: Dimension?,
    @SerializedName("layout")
    val layout: Layout?,
    @SerializedName("detectedLanguages")
    val detectedLanguages: List<DetectedLanguage>?,
    @SerializedName("blocks")
    val blocks: List<Block>?,
    @SerializedName("paragraphs")
    val paragraphs: List<Paragraph>?,
    @SerializedName("lines")
    val lines: List<Line>?,
    @SerializedName("tokens")
    val tokens: List<Token>?
)