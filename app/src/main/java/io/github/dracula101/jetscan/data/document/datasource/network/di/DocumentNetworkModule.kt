package io.github.dracula101.jetscan.data.document.datasource.network.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.document.datasource.network.api.PdfToolApi
import io.github.dracula101.jetscan.data.document.datasource.network.interceptors.UserInfoInterceptor
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepository
import io.github.dracula101.jetscan.data.document.datasource.network.repository.PdfToolRepositoryImpl
import io.github.dracula101.jetscan.data.document.datasource.network.retrofit.DocumentRetrofit
import io.github.dracula101.jetscan.data.document.datasource.network.retrofit.DocumentRetrofitImpl
import io.github.dracula101.jetscan.data.platform.datasource.network.interceptors.BaseUrlInterceptors
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DocumentNetworkModule {

    @Provides
    @Singleton
    fun provideUserInfoInterceptor(): UserInfoInterceptor = UserInfoInterceptor()


    @Provides
    @Singleton
    fun provideDocumentRetrofit(
        userInfoInterceptor: UserInfoInterceptor,
        baseUrlInterceptors: BaseUrlInterceptors,
        gson: Gson
    ): DocumentRetrofit {
        return DocumentRetrofitImpl(
            userInfoInterceptor = userInfoInterceptor,
            baseUrlInterceptors = baseUrlInterceptors,
            gson = gson
        )
    }

    @Provides
    @Singleton
    fun providePdfToolApiRetrofit(documentRetrofit: DocumentRetrofit): PdfToolApi {
        return documentRetrofit
            .pdfToolApiRetrofit
            .create(PdfToolApi::class.java)
    }

    @Provides
    @Singleton
    fun providePdfToolRepository(
        pdfToolApi: PdfToolApi,
        userInfoInterceptor: UserInfoInterceptor,
        authRepository: AuthRepository,
        remoteStorageRepository: RemoteStorageRepository
    ): PdfToolRepository {
        return PdfToolRepositoryImpl(
            pdfToolApi = pdfToolApi,
            userInfoInterceptor = userInfoInterceptor,
            authRepository = authRepository,
            remoteStorageRepository = remoteStorageRepository
        )
    }
}