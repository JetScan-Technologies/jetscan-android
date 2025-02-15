package io.github.dracula101.jetscan.data.auth.datasource.disk

import kotlinx.coroutines.flow.Flow

interface AuthDiskSource {

    val guestTokenFlow: Flow<String?>

    fun getGuestToken(): String?

    fun isGuestLoggedIn(): Boolean

    suspend fun setGuestLoginIn(): String

    fun clearGuestLogin()

    suspend fun saveFirebaseToken(token: String)

    suspend fun addFirebaseToken()
}