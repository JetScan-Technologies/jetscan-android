package io.github.dracula101.jetscan.presentation.platform.di


import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DependencyModule {

    // ==================== Firebase Auth ====================
    @Provides
    @Singleton
    fun providesFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // ==================== Firebase Firestore ====================
    @Provides
    @Singleton
    fun providesFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // ==================== Firebase Storage ====================
    @Provides
    @Singleton
    fun providesFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    // ==================== Google One Tap Client ====================
    @Provides
    @Singleton
    fun providesGoogleClient(@ApplicationContext context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }

    // ==================== Remote Config ====================
    @Provides
    @Singleton
    fun providesFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1200)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        return mFirebaseRemoteConfig
    }
}