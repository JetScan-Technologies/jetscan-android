package io.github.dracula101.jetscan.data.ocr.datasource.disk.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSource
import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSourceImpl
import io.github.dracula101.jetscan.data.platform.datasource.disk.di.EncryptedPreferences
import io.github.dracula101.jetscan.data.platform.datasource.disk.di.UnencryptedPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrDiskModule {

    @Provides
    @Singleton
    fun provideOcrTokenDiskSource(
        @UnencryptedPreferences sharedPreferences: SharedPreferences,
        @EncryptedPreferences encryptedSharedPreferences: SharedPreferences,
    ): TokenDiskSource {
        return TokenDiskSourceImpl(
            sharedPreferences = sharedPreferences,
            encryptedSharedPreferences = encryptedSharedPreferences
        )
    }

}