package io.github.dracula101.jetscan.data.ocr.repository.models.ocr

import android.os.Parcelable
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr.OcrDocumentResponse
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr.Page
import kotlinx.parcelize.Parcelize

@Parcelize
data class OcrResult (
    val text: String?,
    val pages: List<AnnotatedPage>?
) : Parcelable

@Parcelize
data class AnnotatedPage (
    val annotations: List<TextAnnotation>?,
    val pageNumber: Int?,
    val width: Int?,
    val height: Int?
) : Parcelable

@Parcelize
data class TextAnnotation (
    val text: String?,
    val boundingBox: BoundingBox?,
) : Parcelable

@Parcelize
data class BoundingBox (
    val vertices: List<Vertex>?
) : Parcelable

@Parcelize
data class Vertex (
    val normX: Float?,
    val normY: Float?
) : Parcelable


fun OcrDocumentResponse.toOcrResult(): OcrResult {
    return OcrResult(
        text = ocrData?.text,
        pages = ocrData?.pages?.map { it.toAnnotatedPage(ocrData.text ?: "") }
    )
}

private fun Page.toAnnotatedPage(text: String): AnnotatedPage {
    return AnnotatedPage(
        pageNumber = pageNumber,
        width = dimension?.width,
        height = dimension?.height,
        annotations = lines?.flatMap { line->
            line.layout?.textAnchor?.textSegments?.map { textSegment ->
                TextAnnotation(
                    text = text.substring(
                        textSegment.startIndex?.toIntOrNull() ?: 0,
                        textSegment.endIndex?.toIntOrNull() ?: 0
                    ),
                    boundingBox = BoundingBox(
                        vertices = line.layout.boundingPoly?.normalizedVertices?.map {
                            Vertex(normX = it.x?.toFloat(),normY = it.y?.toFloat())
                        }
                    )
                )
            }.orEmpty()
        }
    )
}


