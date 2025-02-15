package io.github.dracula101.jetscan.data.auth.repository

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.data.auth.datasource.disk.AuthDiskSource
import io.github.dracula101.jetscan.data.auth.model.GoogleSignInResult
import io.github.dracula101.jetscan.data.auth.model.LoginResult
import io.github.dracula101.jetscan.data.auth.model.RegisterResult
import io.github.dracula101.jetscan.data.auth.model.UpdateProfileResult
import io.github.dracula101.jetscan.data.auth.model.UserState
import io.github.dracula101.jetscan.data.auth.util.getErrorMessage
import io.github.dracula101.jetscan.data.auth.util.toUserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val authDiskSource: AuthDiskSource,
) : AuthRepository {

    override val currentUser: UserState?
        get() = authDiskSource.getGuestToken()?.let {
            UserState(
                displayName = "Guest",
                email = "",
                photoUrl = "",
                uid = it,
                isGuest = true,
            )
        } ?: firebaseAuth.currentUser?.toUserState()

    private val currentScope = CoroutineScope(Dispatchers.IO)

    private val _firebaseAuthFlow = MutableStateFlow(firebaseAuth.currentUser)

    private val _authStateFlow = MutableStateFlow(currentUser)
    override val authStateFlow: Flow<UserState?> = _authStateFlow
        .onSubscription { emit(currentUser) }
        .map {
            if (it != null) { authDiskSource.addFirebaseToken() }
            it
        }

    init {
        firebaseAuth.addAuthStateListener { authChange ->
            val authUser = authChange.currentUser
            if (authUser != null) {
                authDiskSource.clearGuestLogin()
            }
            _firebaseAuthFlow.value = authUser
        }
        combine(
            _firebaseAuthFlow,
            authDiskSource.guestTokenFlow
        ) { firebaseUser, token ->
            token?.let {
                UserState(
                    displayName = "Guest",
                    email = "",
                    photoUrl = "",
                    uid = it,
                    isGuest = true,
                )
            } ?: firebaseUser?.toUserState()
        }.distinctUntilChanged().onEach {
            Timber.d("Auth State: $it")
            _authStateFlow.value = it
        }.launchIn(currentScope)
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            LoginResult.Success(authResult.user!!.toUserState())
        } catch (error: Exception) {
            Timber.e(error)
            when (error) {
                is FirebaseAuthException -> {
                    val errorMessage = error.getErrorMessage()
                    LoginResult.Error(errorMessage)
                }

                is CancellationException -> {
                    LoginResult.Error(
                        error.localizedMessage ?: "An unexpected error occurred",
                        "CANCELLED"
                    )
                }

                is FirebaseException -> {
                    val errorMessage = error.getErrorMessage()
                    LoginResult.Error(errorMessage)
                }

                else -> {
                    error.printStackTrace()
                    LoginResult.Error(
                        error.localizedMessage ?: "An unexpected error occurred",
                        "UNKNOWN_ERROR"
                    )
                }
            }
        }
    }

    override suspend fun guestLogin(): LoginResult {
        return try {
            val guestToken = authDiskSource.setGuestLoginIn()
            LoginResult.Success(
                UserState(
                    displayName = "Guest",
                    email = "",
                    photoUrl = "",
                    uid = guestToken,
                    isGuest = true,
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            LoginResult.Error(e.localizedMessage ?: "An unexpected error occurred", "UNKNOWN_ERROR")
        }
    }

    override suspend fun loginPasswordLess(): LoginResult {
        return try {
            val authUser = firebaseAuth.signInAnonymously().await()
            LoginResult.Success(authUser.user!!.toUserState())
        } catch (e: Exception) {
            Timber.e(e)
            LoginResult.Error(e.localizedMessage ?: "An unexpected error occurred", "UNKNOWN_ERROR")
        }
    }

    private fun buildGoogleSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    override suspend fun signInWithGoogleOneTapClient(intent: Intent): GoogleSignInResult {
        return try {
            val signInResult = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = signInResult.googleIdToken
            if (googleIdToken.isNullOrEmpty()) {
                return GoogleSignInResult.Error("Couldn't Authenticate User", "ID_TOKEN_EMPTY")
            }
            val googleAuthCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(googleAuthCredential).await()
            if (authResult.user == null) {
                return GoogleSignInResult.Error("Couldn't Authenticate User", "AUTH_FAIL")
            } else {
                GoogleSignInResult.Success(googleIdToken)
            }
        } catch (e: Exception) {
            Timber.e(e)
            GoogleSignInResult.Error(
                e.localizedMessage ?: "An unexpected error occurred",
                "UNKNOWN_ERROR"
            )
        }
    }

    override suspend fun signInWithGooglePlayServices(intent: Intent): GoogleSignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)!!
            if (account.idToken.isNullOrEmpty()) {
                return GoogleSignInResult.Error("Couldn't get Token", "ID_TOKEN_EMPTY")
            }
            val googleAuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(googleAuthCredential).await()
            if (authResult.user == null) {
                return GoogleSignInResult.Error("Couldn't Authenticate User", "AUTH_FAIL")
            } else {
                GoogleSignInResult.Success(account.idToken!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GoogleSignInResult.Error(
                e.localizedMessage ?: "An unexpected error occurred",
                "UNKNOWN_ERROR"
            )
        }
    }

    override fun getGoogleSignInClient(): GoogleSignInClient {
        return try {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .requestProfile()
                    .build()
            )
        } catch (e: Exception) {
            Timber.e(e)
            throw CancellationException(e.message)
        }
    }

    override suspend fun getGoogleSignInIntentOneTapClient(): IntentSender? {
        return try {
            val signInResult = oneTapClient.beginSignIn(buildGoogleSignInRequest()).await()
            signInResult.pendingIntent.intentSender
        } catch (e: Exception) {
            Timber.e(e)
            throw CancellationException(e.message)
        }
    }

    override suspend fun register(name: String, email: String, password: String): RegisterResult {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            )?.await()
            RegisterResult.Success(user!!.toUserState())
        } catch (e: Exception) {
            Timber.e(e)
            when (e) {
                is FirebaseAuthException -> {
                    val errorMessage = e.getErrorMessage()
                    RegisterResult.Error(errorMessage)
                }

                is CancellationException -> {
                    RegisterResult.Error(
                        e.localizedMessage ?: "An unexpected error occurred",
                        "CANCELLED"
                    )
                }

                is FirebaseException -> {
                    val errorMessage = e.getErrorMessage()
                    RegisterResult.Error(errorMessage)
                }

                else -> {
                    RegisterResult.Error(
                        e.localizedMessage ?: "An unexpected error occurred",
                        "UNKNOWN_ERROR"
                    )
                }
            }
        }
    }

    override suspend fun setAvatar(): UpdateProfileResult {
        return try {
            val user = firebaseAuth.currentUser
            val avatarUrl =
                Uri.parse(AVATAR_URL + "?seed=${Uri.encode(user?.displayName ?: "")}")
            val profileUpdates = UserProfileChangeRequest
                .Builder()
                .setDisplayName(user?.displayName)
                .setPhotoUri(avatarUrl)
                .build()
            user?.updateProfile(profileUpdates)?.await()
            UpdateProfileResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateProfileResult.Error(
                e.localizedMessage ?: "An unexpected error occurred",
                "UPDATE_FAIL"
            )
        }
    }

    override suspend fun setUserName(name: String): UpdateProfileResult {
        return try {
            val user = firebaseAuth.currentUser.let {
                it ?: return UpdateProfileResult.Error("User not logged in", "NO_USER")
            }
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            ).await()
            UpdateProfileResult.Success
        } catch (e: Exception) {
            Timber.e(e)
            UpdateProfileResult.Error(
                e.localizedMessage ?: "An unexpected error occurred",
                "UPDATE_FAIL"
            )
        }
    }

    override suspend fun logout() {
        oneTapClient.signOut().await()
        firebaseAuth.signOut()
    }

    companion object {
        const val AVATAR_URL: String = "https://api.dicebear.com/7.x/micah/png"
    }

}



