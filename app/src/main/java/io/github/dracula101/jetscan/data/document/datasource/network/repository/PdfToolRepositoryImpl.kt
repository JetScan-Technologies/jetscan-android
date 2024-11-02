package io.github.dracula101.jetscan.data.document.datasource.network.repository

import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.document.datasource.network.api.PdfToolApi
import io.github.dracula101.jetscan.data.document.datasource.network.interceptors.UserInfoInterceptor
import io.github.dracula101.jetscan.data.document.datasource.network.repository.models.PdfMergeResult
import io.github.dracula101.jetscan.data.platform.repository.remote_storage.RemoteStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

class PdfToolRepositoryImpl(
    private val pdfToolApi: PdfToolApi,
    private val userInfoInterceptor: UserInfoInterceptor,
    private val authRepository: AuthRepository,
    private val remoteStorageRepository: RemoteStorageRepository
) : PdfToolRepository {

    private val unconfinedScope = CoroutineScope(Dispatchers.Unconfined)

    init {
        authRepository
            .authStateFlow
            .onEach { userInfo ->
                userInfoInterceptor.userId = userInfo?.uid
            }
            .launchIn(unconfinedScope)
    }

    override suspend fun mergePdfFiles(
        files: List<File>,
        fileName: String,
        outputFile: File
    ): PdfMergeResult {
        return try {
            val filesPart = files.map { file ->
                MultipartBody.Part.createFormData(
                    name = "files",
                    filename = file.name,
                    body = file.asRequestBody("application/pdf".toMediaType())
                )
            }
            val mergeResponse = pdfToolApi.merge(filesPart).getOrThrow()
            if (mergeResponse.path.isNullOrEmpty()) {
                return PdfMergeResult.Error("Failed to merge PDF files", Exception("Server error"))
            }
            remoteStorageRepository.getFileFromPath(mergeResponse.path, outputFile)
            PdfMergeResult.Success(outputFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to merge PDF files")
            PdfMergeResult.Error("Failed to merge PDF files", e)
        }
    }

}