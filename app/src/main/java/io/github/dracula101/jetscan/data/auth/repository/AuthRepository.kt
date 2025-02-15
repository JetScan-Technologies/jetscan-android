package io.github.dracula101.jetscan.data.auth.repository

import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.github.dracula101.jetscan.data.auth.model.GoogleSignInResult
import io.github.dracula101.jetscan.data.auth.model.LoginResult
import io.github.dracula101.jetscan.data.auth.model.RegisterResult
import io.github.dracula101.jetscan.data.auth.model.UpdateProfileResult
import io.github.dracula101.jetscan.data.auth.model.UserState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /*
    * Returns the current User
    */
    val currentUser: UserState?

    /*
    * Returns the current User State Flow
    * */
    val authStateFlow: Flow<UserState?>

    /*
    * Check if the user is logged in
     */
    fun isLoggedIn(): Boolean

    /*
    * Login with email and password
     */
    suspend fun login(email: String, password: String): LoginResult

    /*
    * Register with email and password
     */
    suspend fun register(name: String, email: String, password: String): RegisterResult

    /*
    * Login without password
     */
    suspend fun loginPasswordLess(): LoginResult

    /*
    * Guest Login
     */
    suspend fun guestLogin(): LoginResult

    /*
    * Set the avatar of the user
     */
    suspend fun setAvatar(): UpdateProfileResult

    /*
    * Set the username of the user
     */
    suspend fun setUserName(name: String): UpdateProfileResult

    /*
    * Get the Google Sign In Client
     */
    fun getGoogleSignInClient(): GoogleSignInClient

    /*
    * Get the Google Sign In Intent for One Tap Client
     */
    suspend fun getGoogleSignInIntentOneTapClient(): IntentSender?

    /*
    * Sign in with Google Play Services
     */
    suspend fun signInWithGooglePlayServices(intent: Intent): GoogleSignInResult

    /*
    * Sign in with Google One Tap Client
     */
    suspend fun signInWithGoogleOneTapClient(intent: Intent): GoogleSignInResult


    /*
    * Logout the user
     */
    suspend fun logout()
}