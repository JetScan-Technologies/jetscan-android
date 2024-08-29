package io.github.dracula101.jetscan.data.platform.datasource.disk.di

import android.content.SharedPreferences
import javax.inject.Qualifier

/**
 * Used to denote an instance of [SharedPreferences] that does not encrypt its data.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnencryptedPreferences

/**
 * Used to denote an instance of [SharedPreferences] that encrypts its data.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EncryptedPreferences