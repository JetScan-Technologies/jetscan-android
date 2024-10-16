package io.github.dracula101.jetscan.data.ocr.datasource.network.retrofit

import retrofit2.Retrofit

interface OcrRetrofit {

    /**
     *  The Retrofit instance for getting the token from the GCP Auth server.
     */
    val ocrTokenApiRetrofit: Retrofit

    /**
     *  The Retrofit instance for getting the OCR data from the GCP Vision server.
     *  This instance is used to send the image to the server and get the OCR data.
     */
    val ocrApiRetrofit: Retrofit

}