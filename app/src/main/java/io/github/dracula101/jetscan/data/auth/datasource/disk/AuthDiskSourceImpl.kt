package io.github.dracula101.jetscan.data.auth.datasource.disk

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.SharedPreferences
import android.os.Build
import io.github.dracula101.jetscan.data.platform.datasource.disk.BaseDiskSource
import io.github.dracula101.jetscan.data.platform.repository.util.bufferedMutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import android.provider.Settings;
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import io.github.dracula101.jetscan.BuildConfig
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthDiskSourceImpl(
    sharedPreferences: SharedPreferences,
    private val contentResolver: ContentResolver,
    private val firebaseDatastore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
): AuthDiskSource, BaseDiskSource(sharedPreferences) {

    private val token = bufferedMutableSharedFlow<String?>(replay = 1)

    override val guestTokenFlow = token.asSharedFlow()

    init {
        token.tryEmit(getString(GUEST_TOKEN))
    }

    override fun getGuestToken(): String? = getString(GUEST_TOKEN)

    override fun isGuestLoggedIn(): Boolean = getString(GUEST_TOKEN) != null

    @SuppressLint("HardwareIds")
    private fun generateUniqueToken(): String? {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override suspend fun setGuestLoginIn(): String {
        val uniqueToken = generateUniqueToken()
        putString(GUEST_TOKEN, uniqueToken)
        token.emit(uniqueToken)
        return uniqueToken ?: ""
    }

    override fun clearGuestLogin() {
        removeWithPrefix(GUEST_TOKEN)
        token.tryEmit(null)
    }

    override suspend fun saveFirebaseToken(token: String) {
        val userCollection = firebaseDatastore.collection("users")
        val userId = generateUniqueToken() ?: return
        val user = userCollection.document(userId)
        user.set(buildMap(token)).await()
    }

    override suspend fun addFirebaseToken() {
        val userCollection = firebaseDatastore.collection("users")
        val userId = generateUniqueToken() ?: return
        val user = userCollection.document(userId)
        val isAdded = getBoolean(ADDED_TOKEN, false) ?: false
        val fcmToken = firebaseMessaging.token.await()
        if (!isAdded) {
            user.set(buildMap(fcmToken)).await()
            putBoolean(ADDED_TOKEN, true)
        }
    }

    private fun buildMap(token: String): Map<String, Any> {
        return hashMapOf(
            "token" to token,
            "device" to Build.MANUFACTURER + " " + Build.MODEL,
            "os" to "Android " + Build.VERSION.RELEASE,
            "app_version" to BuildConfig.VERSION_NAME,
            "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
    }

    companion object {
        const val GUEST_TOKEN = "guest_token"
        const val ADDED_TOKEN = "added_token"
    }

}