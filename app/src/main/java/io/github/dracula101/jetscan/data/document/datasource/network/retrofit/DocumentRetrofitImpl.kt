package io.github.dracula101.jetscan.data.document.datasource.network.retrofit

import android.util.Log
import com.google.gson.Gson
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.document.datasource.network.interceptors.UserInfoInterceptor
import io.github.dracula101.jetscan.data.platform.datasource.network.core.ResultCallAdapterFactory
import io.github.dracula101.jetscan.data.platform.datasource.network.interceptors.BaseUrlInterceptors
import io.github.dracula101.jetscan.data.platform.datasource.network.util.HEADER_KEY_AUTHORIZATION
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DocumentRetrofitImpl(
    private val baseUrlInterceptors: BaseUrlInterceptors,
    private val userInfoInterceptor: UserInfoInterceptor,
    private val gson: Gson,
): DocumentRetrofit {


    override val pdfToolApiRetrofit: Retrofit by lazy { pdfToolRetrofit }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            message.chunked(MAX_LOG_MESSAGE_LENGTH).forEach {
                Log.d("JetScanNetworkClient", it)
            }
        }.apply {
            redactHeader(name = HEADER_KEY_AUTHORIZATION)
            setLevel(
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
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
    private val pdfToolOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(baseUrlInterceptors.apiInterceptor)
            .addInterceptor(userInfoInterceptor)
            .build()
    }

    // ===================== RETROFIT BUILDERS =====================

    /**
     * The [Retrofit.Builder] used to create the Retrofit instance for the GCP Auth server.
     */
    private val pdfToolRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .client(pdfToolOkHttpClient)

    // ===================== RETROFIT INSTANCES =====================

    private val pdfToolRetrofit: Retrofit by lazy {
        pdfToolRetrofitBuilder
            .baseUrl(BuildConfig.JETSCAN_BACKEND_URL)
            .build()
    }
}

private const val MAX_LOG_MESSAGE_LENGTH = 4000