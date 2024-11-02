package io.github.dracula101.jetscan.data.document.datasource.network.api

import io.github.dracula101.jetscan.data.document.datasource.network.models.PdfCompressResponse
import io.github.dracula101.jetscan.data.document.datasource.network.models.PdfCompressSizesResponse
import io.github.dracula101.jetscan.data.document.datasource.network.models.PdfMergeResponse
import io.github.dracula101.jetscan.data.document.datasource.network.models.PdfSplitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PdfToolApi {

    @Multipart
    @POST("api/pdf/merge")
    suspend fun merge(
        @Part files: List<MultipartBody.Part>,
    ): Result<PdfMergeResponse>

    @Multipart
    @POST("api/pdf/split")
    suspend fun split(
        @Part file: MultipartBody.Part,
        @Part ranges: MultipartBody.Part,
    ): Result<PdfSplitResponse>


    @Multipart
    @POST("api/pdf/compress")
    suspend fun compress(
        @Part file: MultipartBody.Part,
        @Part quality: MultipartBody.Part,
    ): Result<PdfCompressResponse>


    @Multipart
    @POST("api/pdf/compress_sizes")
    suspend fun compressSizes(
        @Part file: MultipartBody.Part,
        @Part qualities: MultipartBody.Part,
    ): Result<PdfCompressSizesResponse>
}