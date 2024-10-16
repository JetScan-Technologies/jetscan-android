package io.github.dracula101.jetscan.data.ocr.manager.token

import io.github.dracula101.jetscan.data.ocr.manager.exception.TokenExpiredException

interface OcrTokenManager {

    /**
     *  Function to get the access token.
     *  @return The access token.
     *  @throws TokenExpiredException If the token has expired.
     */
    fun getAccessToken(): String?

    /**
     *  Function to save the access token.
     *  @param token The access token.
     *  @param expiry The expiry time of the token.
     *  @return True if the token was saved successfully, false otherwise.
     */
    fun saveAccessToken(
        token: String,
        tokenType: String,
        expiry: Long,
        tokenStoredTime: Long
    ): Boolean

}