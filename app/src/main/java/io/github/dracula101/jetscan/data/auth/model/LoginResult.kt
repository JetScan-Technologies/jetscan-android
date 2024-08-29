package io.github.dracula101.jetscan.data.auth.model

/**
 * Models result of logging in.
 */
sealed class LoginResult {
    /**
     * Login succeeded.
     */
    data class Success(val userState: UserState) : LoginResult()

    /**
     * There was an error logging in.
     */
    data class Error(val errorMessage: String?, val errorCode: String? = null ) : LoginResult()
}