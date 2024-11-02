package io.github.dracula101.jetscan.data.ocr.datasource.network.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import com.google.gson.ToNumberStrategy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrTokenApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.interceptors.AuthTokenInterceptor
import io.github.dracula101.jetscan.data.platform.datasource.network.interceptors.BaseUrlInterceptors
import io.github.dracula101.jetscan.data.ocr.datasource.network.retrofit.OcrRetrofit
import io.github.dracula101.jetscan.data.ocr.datasource.network.retrofit.OcrRetrofitImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrNetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrlInterceptors(): BaseUrlInterceptors = BaseUrlInterceptors()

    @Provides
    @Singleton
    fun provideAuthTokenInterceptor(): AuthTokenInterceptor = AuthTokenInterceptor()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .serializeNulls()
        .create()

    @Provides
    @Singleton
    fun provideOcrRetrofit(
        authTokenInterceptor: AuthTokenInterceptor,
        baseUrlInterceptors: BaseUrlInterceptors,
        gson: Gson
    ): OcrRetrofit {
        return OcrRetrofitImpl(
            authTokenInterceptor = authTokenInterceptor,
            baseUrlInterceptors = baseUrlInterceptors,
            gson = gson
        )
    }

    @Provides
    @Singleton
    fun provideOcrTokenApi(
        ocrRetrofit: OcrRetrofit
    ): OcrTokenApi {
        return ocrRetrofit
            .ocrTokenApiRetrofit
            .create(OcrTokenApi::class.java)
    }


    @Provides
    @Singleton
    fun provideOcrApi(
        ocrRetrofit: OcrRetrofit
    ): OcrApi {
        return ocrRetrofit
            .ocrApiRetrofit
            .create(OcrApi::class.java)
    }
}