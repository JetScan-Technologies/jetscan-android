package io.github.dracula101.jetscan.data.platform.manager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.platform.manager.special_circumstance.SpecialCircumstanceManager
import io.github.dracula101.jetscan.data.platform.manager.special_circumstance.SpecialCircumstanceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformManagerModule {

    @Provides
    @Singleton
    fun provideSpecialCircumstanceManager(
        authRepository: AuthRepository,
    ): SpecialCircumstanceManager {
        return SpecialCircumstanceManagerImpl(
            authRepository = authRepository,
        )
    }

}