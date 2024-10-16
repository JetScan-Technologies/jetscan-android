package io.github.dracula101.jetscan.data.ocr.manager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSource
import io.github.dracula101.jetscan.data.ocr.manager.token.OcrTokenManager
import io.github.dracula101.jetscan.data.ocr.manager.token.OcrTokenManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrManagerModule {

    @Provides
    @Singleton
    fun provideOcrTokenManager(
        tokenDiskSource: TokenDiskSource
    ): OcrTokenManager {
        return OcrTokenManagerImpl(
            tokenDiskSource = tokenDiskSource
        )
    }

}