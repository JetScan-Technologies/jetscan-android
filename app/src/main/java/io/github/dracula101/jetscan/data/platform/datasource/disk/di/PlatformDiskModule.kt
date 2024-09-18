package io.github.dracula101.jetscan.data.platform.datasource.disk.di


import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.platform.datasource.disk.SettingsDiskSource
import io.github.dracula101.jetscan.data.platform.datasource.disk.SettingsDiskSourceImpl
import javax.inject.Singleton

/**
 * Provides persistence-related dependencies in the platform package.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlatformDiskModule {

    @Provides
    @Singleton
    fun provideSettingsDiskSource(
        @UnencryptedPreferences sharedPreferences: SharedPreferences,
    ): SettingsDiskSource =
        SettingsDiskSourceImpl(
            sharedPreferences = sharedPreferences,
        )
}
