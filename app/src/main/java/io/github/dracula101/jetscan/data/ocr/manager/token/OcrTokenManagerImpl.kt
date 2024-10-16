package io.github.dracula101.jetscan.data.ocr.manager.token

import io.github.dracula101.jetscan.data.ocr.datasource.disk.token.TokenDiskSource
import io.github.dracula101.jetscan.data.ocr.manager.exception.TokenExpiredException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class OcrTokenManagerImpl(
    private val tokenDiskSource: TokenDiskSource
) : OcrTokenManager {

    private val mutableAccessTokenStateFlow: MutableStateFlow<String?> = MutableStateFlow(tokenDiskSource.token)
    private val mutableTokenTypeStateFlow: MutableStateFlow<String?> = MutableStateFlow(tokenDiskSource.tokenType)

    private val ocrManagerScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    init {
        tokenDiskSource.tokenStateFlow
            .onEach { mutableAccessTokenStateFlow.value = it }
            .launchIn(ocrManagerScope)
        tokenDiskSource.tokenTypeStateFlow
            .onEach { mutableTokenTypeStateFlow.value = it }
            .launchIn(ocrManagerScope)

    }

    @Throws(TokenExpiredException::class)
    override fun getAccessToken(): String {
        // check if the token has expired
        val tokenStoredTime = tokenDiskSource.tokenStoredTime ?: 0
        val currentTime = System.currentTimeMillis() / 1000
        val expiry = tokenDiskSource.tokenExpiry ?: 0
        if (tokenStoredTime + expiry < currentTime) { throw TokenExpiredException() }

        return mutableAccessTokenStateFlow.value
            ?: tokenDiskSource.token
            ?: throw TokenExpiredException()
    }

    override fun saveAccessToken(
        token: String,
        tokenType: String,
        expiry: Long,
        tokenStoredTime: Long
    ): Boolean {
        tokenDiskSource.tokenStoredTime = tokenStoredTime
        tokenDiskSource.token = token
        tokenDiskSource.tokenExpiry = expiry
        return true
    }
}