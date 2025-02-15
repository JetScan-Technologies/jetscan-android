package io.github.dracula101.jetscan.data.auth.datasource.disk.di

import android.content.ContentResolver
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSource
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSourceImpl
import io.github.dracula101.jetscan.data.platform.datasource.disk.di.UnencryptedPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthDiskModule {

    @Provides
    @Singleton
    fun provideAuthDiskSource(
        @UnencryptedPreferences sharedPref: SharedPreferences,
        contentResolver: ContentResolver,
        firebaseFirestore: FirebaseFirestore,
        firebaseMessaging: FirebaseMessaging
    ): AuthDiskSource {
        return AuthDiskSourceImpl(
            sharedPreferences = sharedPref,
            contentResolver = contentResolver,
            firebaseDatastore = firebaseFirestore,
            firebaseMessaging = firebaseMessaging
        )
    }

}