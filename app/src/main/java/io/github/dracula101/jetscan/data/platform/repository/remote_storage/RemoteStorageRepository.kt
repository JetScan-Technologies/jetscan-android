package io.github.dracula101.jetscan.data.platform.repository.remote_storage

import java.io.File

interface RemoteStorageRepository {

    suspend fun getFileFromPath(
        path: String,
        outputFile: File
    ): Result<Boolean>

    suspend fun getFileFromUrl(
        url: String,
        outputFile: File
    ): Result<Boolean>

    suspend fun putFile(
        path: String,
        file: File
    ): Result<Boolean>

    suspend fun deleteFile(
        path: String
    ): Result<Boolean>

}