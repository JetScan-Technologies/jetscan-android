package io.github.dracula101.jetscan.data.document.datasource.network.models.ocr


import com.google.gson.annotations.SerializedName

data class Page(
    @SerializedName("blocks")
    val blocks: List<Block>?,
    @SerializedName("detectedLanguages")
    val detectedLanguages: List<DetectedLanguage>?,
    @SerializedName("dimension")
    val dimension: Dimension?,
    @SerializedName("image")
    val image: Image?,
    @SerializedName("layout")
    val layout: LayoutX?,
    @SerializedName("lines")
    val lines: List<Line>?,
    @SerializedName("pageNumber")
    val pageNumber: Int?,
    @SerializedName("paragraphs")
    val paragraphs: List<Paragraph>?,
    @SerializedName("tokens")
    val tokens: List<Token>?
)