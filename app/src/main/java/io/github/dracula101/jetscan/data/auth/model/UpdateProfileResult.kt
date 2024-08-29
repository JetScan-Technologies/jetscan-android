package io.github.dracula101.jetscan.data.auth.model


/**
 * Models result of updating profile.
 */
sealed class UpdateProfileResult {
    /**
     * Profile update succeeded.
     */
    data object Success : UpdateProfileResult()

    /**
     * There was an error updating profile.
     */
    data class Error(val errorMessage: String?, val errorCode: String) : UpdateProfileResult()
}