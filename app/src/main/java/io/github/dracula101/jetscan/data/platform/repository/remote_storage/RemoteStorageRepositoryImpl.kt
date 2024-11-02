package io.github.dracula101.jetscan.data.platform.repository.remote_storage

import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

class RemoteStorageRepositoryImpl(
    private val firebaseStorage: FirebaseStorage
) : RemoteStorageRepository {
    
    override suspend fun getFileFromPath(path: String, outputFile: File): Result<Boolean> {
        return try {
            val reference = firebaseStorage.getReference(path)
            reference.getFile(outputFile).await()
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    override suspend fun getFileFromUrl(url: String, outputFile: File): Result<Boolean> {
        return try {
            val fileReference = firebaseStorage.getReferenceFromUrl(url)
            val downloadUrl = fileReference.downloadUrl
            Timber.i("Download Url: $downloadUrl")
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    override suspend fun putFile(path: String, file: File): Result<Boolean> {
        return try {
            val fileReference = firebaseStorage.getReference(path)
            fileReference.putFile(file.toUri()).await()
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(path: String): Result<Boolean> {
        return try {
            val fileReference = firebaseStorage.getReference(path)
            fileReference.delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

}