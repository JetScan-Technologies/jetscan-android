package io.github.dracula101.jetscan.data.ocr.datasource.network.api

import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.DocumentOcrBody
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.ocr.OcrDocumentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OcrApi {

    @POST(BuildConfig.GCP_DOCUMENT_AI_ENDPOINT)
    suspend fun processDocument(
        @Body body: DocumentOcrBody
    ) : Result<OcrDocumentResponse>

}