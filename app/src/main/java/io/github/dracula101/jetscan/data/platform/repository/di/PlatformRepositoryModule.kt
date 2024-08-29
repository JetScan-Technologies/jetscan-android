package io.github.dracula101.jetscan.data.platform.repository.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.platform.datasource.disk.SettingsDiskSource
import io.github.dracula101.jetscan.data.platform.repository.SettingsRepository
import io.github.dracula101.jetscan.data.platform.repository.SettingsRepositoryImpl
import javax.inject.Singleton

/**
 * Provides repositories in the auth package.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlatformRepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDiskSource: SettingsDiskSource,
    ): SettingsRepository =
        SettingsRepositoryImpl(
            settingsDiskSource = settingsDiskSource,
        )
}
