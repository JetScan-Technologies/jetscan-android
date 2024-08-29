package io.github.dracula101.jetscan.data.platform.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManagerImpl
import java.io.File
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.Executor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    // ======================== App Context ========================
    @Provides
    @Singleton
    fun provideAppContext(@ApplicationContext context: Context): Context {
        return context
    }

    // ======================== Main Executor ========================
    @Provides
    @Singleton
    fun provideMainExecutor(@ApplicationContext cxt: Context): Executor {
        return ContextCompat.getMainExecutor(cxt)
    }

    // ======================== Content Resolver ========================
    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    // ======================== Package Manager ========================
    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    // ======================== File System Provider ========================
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideFileSystemProvider(): FileSystemProvider {
        return FileSystemProvider.installedProviders().first()
    }

    // ======================== Local Files Directory ========================
    @Provides
    @Singleton
    fun provideLocalFiles(@ApplicationContext context: Context): File {
        return context.filesDir
    }

    // ======================== Local Cache Directory ========================
    @Provides
    @Singleton
    fun provideLocalCache(@ApplicationContext context: Context): File {
        return context.cacheDir
    }

    // ======================== Image Processing ========================
    @Provides
    @Singleton
    fun provideOpenCvManager(): OpenCvManager {
        return OpenCvManagerImpl()
    }
}