package io.github.dracula101.jetscan.data.auth.di

import android.content.Context
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.datasource.AuthDataSource
import io.github.dracula101.jetscan.data.auth.datasource.AuthDataSourceImpl
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.auth.repository.AuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthDataSource(): AuthDataSource {
        return AuthDataSourceImpl()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        authDataSource: AuthDataSource,
        oneTapClient: SignInClient,
    ): AuthRepository {
        return AuthRepositoryImpl(
            context = context,
            firebaseAuth = firebaseAuth,
            oneTapClient = oneTapClient,
        )
    }
}