package io.github.dracula101.jetscan.data.ocr.repository

import android.util.Base64
import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSource
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.api.OcrTokenApi
import io.github.dracula101.jetscan.data.ocr.datasource.network.interceptors.AuthTokenInterceptor
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.DocumentOcrBody
import io.github.dracula101.jetscan.data.ocr.datasource.network.models.RawDocument
import io.github.dracula101.jetscan.data.ocr.manager.exception.TokenExpiredException
import io.github.dracula101.jetscan.data.ocr.manager.token.OcrTokenManager
import io.github.dracula101.jetscan.data.ocr.repository.exception.OcrNetworkException
import io.github.dracula101.jetscan.data.ocr.repository.models.OcrDocumentResult
import io.github.dracula101.jetscan.data.ocr.repository.models.ocr.OcrResult
import io.github.dracula101.jetscan.data.ocr.repository.models.ocr.toOcrResult
import io.github.dracula101.jetscan.data.ocr.service.JwtTokenService
import io.github.dracula101.jetscan.data.platform.repository.util.bufferedMutableSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class OcrRepositoryImpl(
    private val jwtTokenService: JwtTokenService,
    private val ocrApi: OcrApi,
    private val ocrTokenApi: OcrTokenApi,
    private val authTokenInterceptor: AuthTokenInterceptor,
    private val tokenManager: OcrTokenManager,
    private val tokenDiskSource: TokenDiskSource
) : OcrRepository {

    private val accessTokenFlow = bufferedMutableSharedFlow<String?>()
    private val ocrRepositoryScope = CoroutineScope(Dispatchers.Unconfined)

    init {
        tokenDiskSource
            .tokenStateFlow
            .onEach { token ->
                if (token != null) {
                    accessTokenFlow.tryEmit(token)
                    authTokenInterceptor.authToken = token
                    Timber.d("Token: ${token.substring(0, 10)}"+"*".repeat(25))
                }
            }
            .launchIn(ocrRepositoryScope)
    }


    override suspend fun processImageDocument(
        file: File,
        mimeType: String
    ): OcrDocumentResult<OcrResult> {
        return try {
            val token = try {
                tokenManager.getAccessToken()
            } catch (e: TokenExpiredException) { null }
            if(token == null) {
                val jwtToken = jwtTokenService.getJwtToken()
                ocrTokenApi.getAccessToken(assertion = jwtToken)
                    .onSuccess { tokenResponse ->
                        if (tokenResponse.accessToken == null) { return@onSuccess }
                        if (tokenResponse.expiresIn == null) { return@onSuccess }
                        val tokenSaved = tokenManager.saveAccessToken(
                            token = tokenResponse.accessToken,
                            tokenType = tokenResponse.tokenType ?: "Bearer",
                            expiry = tokenResponse.expiresIn,
                            tokenStoredTime = System.currentTimeMillis() / 1000
                        )
                        if (tokenSaved) {
                            Timber.d("Token saved successfully")
                        }
                    }
            }
            val base64EncodedContent = withContext(Dispatchers.IO) {
                file.inputStream().use { inputStream ->
                    Base64.encodeToString(inputStream.readBytes(), Base64.NO_WRAP)
                }
            }
            val rawDocumentBody = DocumentOcrBody(
                document = RawDocument(
                    content = base64EncodedContent,
                    mimeType = mimeType
                )
            )
            val ocrResult = ocrApi.processDocument(rawDocumentBody)
                .onFailure { throw OcrNetworkException() }
                .getOrNull()
                ?.toOcrResult()
            OcrDocumentResult.Success(
                data = ocrResult ?: OcrResult(text = "",pages = emptyList())
            )
        } catch (e: Exception) {
            Timber.e(e)
            when (e) {
                is OcrNetworkException -> OcrDocumentResult.Error(
                    message = "Network error",
                    exception = OcrNetworkException()
                )
                else -> OcrDocumentResult.Error(
                    message = "Unknown error",
                    exception = e
                )
            }
        }
    }


}