package io.github.dracula101.jetscan.data.auth.util

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

private fun getErrorMessage(errorCode: String): String {
    return when (errorCode) {
        "ERROR_INVALID_CUSTOM_TOKEN" -> "The custom token format is incorrect. Please check the documentation."
        "ERROR_CUSTOM_TOKEN_MISMATCH" -> "The custom token corresponds to a different audience."
        "ERROR_INVALID_EMAIL" -> "The email address is incorrect"
        "ERROR_WRONG_PASSWORD" -> "The password is incorrect"
        "INVALID_LOGIN_CREDENTIALS" -> "The email address or password is incorrect"
        "ERROR_USER_MISMATCH" -> "The supplied credentials do not correspond to the previously signed in user."
        "ERROR_REQUIRES_RECENT_LOGIN" -> "This operation is sensitive and requires recent authentication. Log in again before retrying this request."
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "The email address is already in use by another account."
        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This credential is already associated with a different user account."
        "ERROR_USER_DISABLED" -> "The user account has been disabled."
        "ERROR_USER_TOKEN_EXPIRED" -> "The user\\'s credential is no longer valid."
        "ERROR_USER_NOT_FOUND" -> "There is no user record corresponding to this identifier"
        "ERROR_INVALID_USER_TOKEN" -> "The user\\'s credential is no longer valid. The user must sign in again."
        "ERROR_OPERATION_NOT_ALLOWED" -> "This operation is not allowed. You must enable this service in the console."
        "ERROR_WEAK_PASSWORD" -> "The given password is weak (6 chars min)."
        "ERROR_EXPIRED_ACTION_CODE" -> "The out of band code has expired."
        "ERROR_INVALID_ACTION_CODE" -> "The out of band code is invalid. This can happen if the code is malformed, expired, or has already been used."
        "ERROR_INVALID_MESSAGE_PAYLOAD" -> "The email template corresponding to this action contains invalid characters in its message."
        "ERROR_INVALID_SENDER" -> "The email template corresponding to this action contains an invalid sender email or name."
        else -> "Unknown Error"
    }
}

fun FirebaseAuthException.getErrorMessage(): String {
    return getErrorMessage(this.errorCode)
}

fun FirebaseException.getErrorMessage(): String {
    val errorCode = this.message?.substringAfter("[ ")
        ?.substringBefore(" ]")
        ?.trim()
        ?: "UNKNOWN_ERROR"
    return getErrorMessage(errorCode)
}