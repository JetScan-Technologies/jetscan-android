package io.github.dracula101.jetscan.data.platform.repository.di

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSource
import io.github.dracula101.jetscan.data.platform.datasource.disk.config.ConfigDiskSource
import io.github.dracula101.jetscan.data.platform.datasource.disk.settings.SettingsDiskSource
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepositoryImpl
import io.github.dracula101.jetscan.data.platform.repository.notification.PushNotificationRepository
import io.github.dracula101.jetscan.data.platform.repository.notification.PushNotificationRepositoryImpl
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepository
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepositoryImpl
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepository
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepositoryImpl
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
    fun provideRemoteStorageRepository(
        firebaseStorage: FirebaseStorage,
    ): RemoteStorageRepository {
        return RemoteStorageRepositoryImpl(firebaseStorage =  firebaseStorage)
    }

    @Provides
    @Singleton
    fun providePushNotificationRepository(
        @ApplicationContext context: Context,
        authDiskSource: AuthDiskSource,
    ) : PushNotificationRepository {
        return PushNotificationRepositoryImpl(
            context = context,
            authDiskSource = authDiskSource,
        )
    }
}
