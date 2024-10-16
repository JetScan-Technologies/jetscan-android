package io.github.dracula101.jetscan.data.ocr.datasource.disk.token

import kotlinx.coroutines.flow.Flow

interface TokenDiskSource {

    /**
     *  The token value.
     *  This value is used to authenticate the user with the server via GCP Auth.
     */
    var token: String?
    val tokenStateFlow: Flow<String?>

    /**
     *  The token type.
     *  This value must be "Bearer" from the response.
     */
    var tokenType: String?
    val tokenTypeStateFlow: Flow<String?>

    /**
     *  The token expiry.
     *  This value is used to determine when the token will expire.
     */
    var tokenExpiry: Long?
    val tokenExpiryStateFlow: Flow<Long?>

    /**
     *  The time the token was last stored
     *  This value is used to determine when the token was last stored.
     */
    var tokenStoredTime: Long?
    val tokenStoredTimeStateFlow: Flow<Long?>


}