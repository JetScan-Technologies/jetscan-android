package io.github.dracula101.jetscan.data.document.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentFolderDao
import io.github.dracula101.jetscan.data.document.datasource.disk.database.DocumentDatabase
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.manager.DocumentManagerImpl
import io.github.dracula101.jetscan.data.document.manager.apk.ApkManager
import io.github.dracula101.jetscan.data.document.manager.apk.ApkManagerImpl
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManager
import io.github.dracula101.jetscan.data.document.manager.extension.ExtensionManagerImpl
import io.github.dracula101.jetscan.data.document.manager.file.FileManager
import io.github.dracula101.jetscan.data.document.manager.file.FileManagerImpl
import io.github.dracula101.jetscan.data.document.manager.image.ImageManager
import io.github.dracula101.jetscan.data.document.manager.image.ImageManagerImpl
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManager
import io.github.dracula101.jetscan.data.document.manager.mime.MimeTypeManagerImpl
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManager
import io.github.dracula101.jetscan.data.document.manager.pdf.PdfManagerImpl
import io.github.dracula101.jetscan.data.document.manager.video.VideoManager
import io.github.dracula101.jetscan.data.document.manager.video.VideoManagerImpl
import io.github.dracula101.jetscan.data.document.repository.DocumentRepository
import io.github.dracula101.jetscan.data.document.repository.DocumentRepositoryImpl
import io.github.dracula101.jetscan.data.document.utils.DBConstants
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DocumentModule {

    @Provides
    @Singleton
    fun provideExtensionManager(): ExtensionManager = ExtensionManagerImpl()

    @Provides
    @Singleton
    fun provideMimeTypeManager(): MimeTypeManager = MimeTypeManagerImpl()

    @Provides
    @Singleton
    fun provideImageManager(): ImageManager = ImageManagerImpl()

    @Provides
    @Singleton
    fun providePdfManager(): PdfManager = PdfManagerImpl()

    @Provides
    @Singleton
    fun provideApkManager(): ApkManager = ApkManagerImpl()

    @Provides
    @Singleton
    fun provideVideoManager(): VideoManager = VideoManagerImpl()

    @Provides
    @Singleton
    fun provideDocumentManager(
        @ApplicationContext context: Context,
        extensionManager: ExtensionManager,
        mimeTypeManager: MimeTypeManager,
        imageManager: ImageManager,
        pdfManager: PdfManager,
        apkManager: ApkManager,
        videoManager: VideoManager,
    ): DocumentManager {
        return DocumentManagerImpl(
            context = context,
            extensionManager = extensionManager,
            mimeTypeManager = mimeTypeManager,
            imageManager = imageManager,
            pdfManager = pdfManager,
            apkManager = apkManager,
            videoManager = videoManager,
        )
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        folderDao: DocumentFolderDao,
    ): DocumentRepository = DocumentRepositoryImpl(
        documentDao = documentDao,
        folderDocumentDao = folderDao
    )

    @Provides
    @Singleton
    fun provideFileManager(
        @ApplicationContext context: Context,
        documentManager: DocumentManager,
        extensionManager: ExtensionManager,
        imageManager: ImageManager,
        pdfManager: PdfManager,
    ): FileManager = FileManagerImpl(
        extensionManager = extensionManager,
        imageManager = imageManager,
        pdfManager = pdfManager,
        documentManager = documentManager,
        contentResolver = context.contentResolver,
        fileManagerDirectory = context.filesDir,
    )

    // ======================== Document Database ========================

    @Provides
    @Singleton
    fun provideDocumentDao(documentDatabase: DocumentDatabase): DocumentDao {
        return documentDatabase.documentDao
    }

    @Provides
    @Singleton
    fun provideFoldersDao(documentDatabase: DocumentDatabase): DocumentFolderDao {
        return documentDatabase.foldersDao
    }


    @Provides
    @Singleton
    fun provideScannedDocumentDatabase(@ApplicationContext context: Context): DocumentDatabase {
        return Room
            .databaseBuilder(
                context,
                DocumentDatabase::class.java,
                DBConstants.DocDB.name
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}