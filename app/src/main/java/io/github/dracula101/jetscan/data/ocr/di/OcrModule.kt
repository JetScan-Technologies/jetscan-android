package io.github.dracula101.jetscan.data.ocr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSource
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrTokenApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.interceptors.AuthTokenInterceptor
import io.github.dracula101.jetscan.data.ocr.manager.token.OcrTokenManager
import io.github.dracula101.jetscan.data.ocr.repository.OcrRepository
import io.github.dracula101.jetscan.data.ocr.repository.OcrRepositoryImpl
import io.github.dracula101.jetscan.data.ocr.service.JwtTokenService
import io.github.dracula101.jetscan.data.ocr.service.JwtTokenServiceImpl
import io.github.dracula101.jetscan.data.ocr.util.PemUtils
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    @Singleton
    fun providesPemUtils(): PemUtils = PemUtils()

    @Provides
    @Singleton
    fun providesJwtTokenService(
        pemUtils: PemUtils
    ): JwtTokenService {
        return JwtTokenServiceImpl(
            pemUtils = pemUtils
        )
    }

    @Provides
    @Singleton
    fun providesOcrRepository(
        jwtTokenService: JwtTokenService,
        ocrApi: OcrApi,
        ocrTokenApi: OcrTokenApi,
        authTokenInterceptor: AuthTokenInterceptor,
        tokenDiskSource: TokenDiskSource,
        tokenManager: OcrTokenManager
    ): OcrRepository {
        return OcrRepositoryImpl(
            jwtTokenService = jwtTokenService,
            ocrApi = ocrApi,
            ocrTokenApi = ocrTokenApi,
            authTokenInterceptor = authTokenInterceptor,
            tokenDiskSource = tokenDiskSource,
            tokenManager = tokenManager,
        )
    }

}