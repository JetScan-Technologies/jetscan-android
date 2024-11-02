package io.github.dracula101.jetscan.data.platform.repository.di

import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.platform.datasource.disk.config.ConfigDiskSource
import io.github.dracula101.jetscan.data.platform.datasource.disk.settings.SettingsDiskSource
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepositoryImpl
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepository
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepositoryImpl
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepository
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepositoryImpl
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

    @Provides
    @Singleton
    fun provideConfigRepository(
        configDiskSource: ConfigDiskSource,
    ): ConfigRepository =
        ConfigRepositoryImpl(
            configDiskSource = configDiskSource,
        )

    @Provides
    @Singleton
    fun provideRemoteStorageRepository(): RemoteStorageRepository =
        RemoteStorageRepositoryImpl(
            firebaseStorage = FirebaseStorage.getInstance(),
        )

}
