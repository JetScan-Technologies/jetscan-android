package io.github.dracula101.jetscan.data.ocr.datasource.network.retrofit

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.ocr.datasource.network.interceptors.AuthTokenInterceptor
import io.github.dracula101.jetscan.data.platform.datasource.network.interceptors.BaseUrlInterceptors
import io.github.dracula101.jetscan.data.platform.datasource.network.core.ResultCallAdapterFactory
import io.github.dracula101.jetscan.data.platform.datasource.network.util.HEADER_KEY_AUTHORIZATION
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@SuppressLint("LogNotTimber")
class OcrRetrofitImpl(
    authTokenInterceptor: AuthTokenInterceptor,
    baseUrlInterceptors: BaseUrlInterceptors,
    gson: Gson,
): OcrRetrofit {


    override val ocrTokenApiRetrofit: Retrofit by lazy { gcpAuthRetrofit }

    override val ocrApiRetrofit: Retrofit by lazy { gcpOcrRetrofit }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            message.chunked(MAX_LOG_MESSAGE_LENGTH).forEach {
                Log.d("JetScanNetworkClient", it)
            }
        }.apply {
            redactHeader(name = HEADER_KEY_AUTHORIZATION)
            setLevel(
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.HEADERS
                } else {
                    HttpLoggingInterceptor.Level.NONE
                },
            )
        }
    }

    // ===================== OK HTTP CLIENTS =====================

    /**
     * The [OkHttpClient] used to make requests to the GCP Auth server.
     * This client is used to get the access token from the GCP Auth server.
     */
    private val gcpAuthOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(baseUrlInterceptors.apiInterceptor)
            .build()
    }

    /**
     * The [OkHttpClient] used to make requests to the GCP Vision server.
     * This client is used to send the image to the server and get the OCR data.
     */
    private val gcpOcrOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(baseUrlInterceptors.apiInterceptor)
            .build()
    }

    // ===================== RETROFIT BUILDERS =====================

    /**
     * The [Retrofit.Builder] used to create the Retrofit instance for the GCP Auth server.
     */
    private val gcpAuthRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .client(gcpAuthOkHttpClient)

    /**
     * The [Retrofit.Builder] used to create the Retrofit instance for the GCP Vision server.
     */
    private val gcpOcrRetrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(gcpOcrOkHttpClient)
    }

    // ===================== RETROFIT INSTANCES =====================

    private val gcpAuthRetrofit: Retrofit by lazy {
        gcpAuthRetrofitBuilder
            .baseUrl("https://oauth2.googleapis.com")
            .build()
    }

    private val gcpOcrRetrofit: Retrofit by lazy {
        gcpOcrRetrofitBuilder
            .baseUrl(BuildConfig.GCP_DOCUMENT_AI_BASE_URL)
            .build()
    }
}

private const val MAX_LOG_MESSAGE_LENGTH: Int = 4000

