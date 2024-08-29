package io.github.dracula101.jetscan.data.auth.model

/*
*  Models result of Google sign in.
*/
sealed class GoogleSignInResult {
    /**
     * Google sign in succeeded.
     */
    data class Success(val idToken: String) : GoogleSignInResult()

    /**
     * There was an error signing in.
     */
    data class Error(val errorMessage: String?, val errorCode: String) : GoogleSignInResult()
}
