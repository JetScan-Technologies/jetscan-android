package io.github.dracula101.jetscan.data.auth.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSource
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSourceImpl
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.auth.repository.AuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        authDiskSource: AuthDiskSource,
        oneTapClient: SignInClient,
    ): AuthRepository {
        return AuthRepositoryImpl(
            context = context,
            firebaseAuth = firebaseAuth,
            oneTapClient = oneTapClient,
            authDiskSource = authDiskSource,
        )
    }
}