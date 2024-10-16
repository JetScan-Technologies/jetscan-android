package io.github.dracula101.jetscan.data.ocr.datasource.disk.token

import android.content.SharedPreferences
import io.github.dracula101.jetscan.data.platform.datasource.disk.BaseEncryptedDiskSource
import io.github.dracula101.jetscan.data.platform.repository.util.bufferedMutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onSubscription

class TokenDiskSourceImpl(
    sharedPreferences: SharedPreferences,
    encryptedSharedPreferences: SharedPreferences,
): BaseEncryptedDiskSource(
    sharedPreferences = sharedPreferences,
    encryptedSharedPreferences = encryptedSharedPreferences
), TokenDiskSource {

    private val accessTokenFlow = bufferedMutableSharedFlow<String?>(replay = 1)
    private val tokenTypeFlow = bufferedMutableSharedFlow<String?>(replay = 1)
    private val tokenExpiryFlow = bufferedMutableSharedFlow<Long?>(replay = 1)
    private val tokenStoredTimeFlow = bufferedMutableSharedFlow<Long?>(replay = 1)

    override var token: String?
        get() = getEncryptedString(ACCESS_TOKEN_KEY)
        set(value) {
            putEncryptedString(ACCESS_TOKEN_KEY, value)
            accessTokenFlow.tryEmit(value)
        }

    override val tokenStateFlow: Flow<String?>
        get() = accessTokenFlow.onSubscription {
            emit(getEncryptedString(ACCESS_TOKEN_KEY))
        }


    override var tokenType: String?
        get() = getEncryptedString(TOKEN_TYPE_KEY)
        set(value) {
            putEncryptedString(TOKEN_TYPE_KEY, value)
            tokenTypeFlow.tryEmit(value)
        }

    override val tokenTypeStateFlow: Flow<String?>
        get() = tokenTypeFlow.onSubscription {
            emit(getEncryptedString(TOKEN_TYPE_KEY))
        }


    override var tokenExpiry: Long?
        get() = getEncryptedString(TOKEN_EXPIRY_KEY)?.toLong()
        set(value) {
            putEncryptedString(TOKEN_EXPIRY_KEY, value.toString())
            tokenExpiryFlow.tryEmit(value)
        }

    override val tokenExpiryStateFlow: Flow<Long?>
        get() = tokenExpiryFlow.onSubscription {
            emit(getEncryptedString(TOKEN_EXPIRY_KEY)?.toLong())
        }


    override var tokenStoredTime: Long?
        get() = getEncryptedString(TOKEN_STORED_TIME_KEY)?.toLong()
        set(value) {
            putEncryptedString(TOKEN_STORED_TIME_KEY, value.toString())
            tokenStoredTimeFlow.tryEmit(value)
        }

    override val tokenStoredTimeStateFlow: Flow<Long?>
        get() = tokenStoredTimeFlow.onSubscription {
            emit(getEncryptedString(TOKEN_STORED_TIME_KEY)?.toLong())
        }


    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val TOKEN_TYPE_KEY = "token_type"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
        private const val TOKEN_STORED_TIME_KEY = "token_stored_time"
    }

}