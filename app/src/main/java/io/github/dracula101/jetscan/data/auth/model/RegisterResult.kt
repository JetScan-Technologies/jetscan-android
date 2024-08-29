package io.github.dracula101.jetscan.data.auth.model

/**
 * Models result of register in.
 */
sealed class RegisterResult {
    /**
     * Register succeeded.
     */
    data class Success(val userState: UserState) : RegisterResult()

    /**
     * There was an error logging in.
     */
    data class Error(val errorMessage: String?, val errorCode: String? = null) : RegisterResult()
}