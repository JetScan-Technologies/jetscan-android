package io.github.dracula101.jetscan.data.platform.datasource.disk


import android.content.SharedPreferences
import com.squareup.moshi.Json
import io.github.dracula101.jetscan.data.platform.repository.util.bufferedMutableSharedFlow
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.encodeToString
import java.time.Instant

private const val APP_THEME_KEY = "theme"
private const val SYSTEM_BIOMETRIC_INTEGRITY_SOURCE_KEY = "biometricIntegritySource"
private const val ACCOUNT_BIOMETRIC_INTEGRITY_VALID_KEY = "accountBiometricIntegrityValid"
private const val HAS_USER_LOGGED_IN_OR_CREATED_AN_ACCOUNT_KEY = "hasUserLoggedInOrCreatedAccount"

/**
 * Primary implementation of [SettingsDiskSource].
 */
class SettingsDiskSourceImpl(
    sharedPreferences: SharedPreferences,
) : BaseDiskSource(sharedPreferences = sharedPreferences),
    SettingsDiskSource {
    private val mutableAppThemeFlow = bufferedMutableSharedFlow<AppTheme>(replay = 1)

    private val mutableHasUserLoggedInOrCreatedAccountFlow = bufferedMutableSharedFlow<Boolean?>()

    override var systemBiometricIntegritySource: String?
        get() = getString(key = SYSTEM_BIOMETRIC_INTEGRITY_SOURCE_KEY)
        set(value) {
            putString(key = SYSTEM_BIOMETRIC_INTEGRITY_SOURCE_KEY, value = value)
        }

    override var appTheme: AppTheme
        get() = getString(key = APP_THEME_KEY)
            ?.let { storedValue ->
                AppTheme.entries.firstOrNull { storedValue == it.value }
            }
            ?: AppTheme.DARK
        set(newValue) {
            putString(
                key = APP_THEME_KEY,
                value = newValue.value,
            )
            mutableAppThemeFlow.tryEmit(appTheme)
        }

    override val appThemeFlow: Flow<AppTheme>
        get() = mutableAppThemeFlow
            .onSubscription { emit(appTheme) }

    override var hasUserLoggedInOrCreatedAccount: Boolean?
        get() = getBoolean(key = HAS_USER_LOGGED_IN_OR_CREATED_AN_ACCOUNT_KEY)
        set(value) {
            putBoolean(key = HAS_USER_LOGGED_IN_OR_CREATED_AN_ACCOUNT_KEY, value = value)
            mutableHasUserLoggedInOrCreatedAccountFlow.tryEmit(value)
        }

    override val hasUserLoggedInOrCreatedAccountFlow: Flow<Boolean?>
        get() = mutableHasUserLoggedInOrCreatedAccountFlow
            .onSubscription { emit(getBoolean(HAS_USER_LOGGED_IN_OR_CREATED_AN_ACCOUNT_KEY)) }

    override fun clearData() {
        removeWithPrefix(prefix = ACCOUNT_BIOMETRIC_INTEGRITY_VALID_KEY)
    }

    override fun getAccountBiometricIntegrityValidity(
        userId: String,
        systemBioIntegrityState: String,
    ): Boolean? =
        getBoolean(
            key = ACCOUNT_BIOMETRIC_INTEGRITY_VALID_KEY
                .appendIdentifier(userId)
                .appendIdentifier(systemBioIntegrityState),
        )

    override fun storeAccountBiometricIntegrityValidity(
        userId: String,
        systemBioIntegrityState: String,
        value: Boolean?,
    ) {
        putBoolean(
            key = ACCOUNT_BIOMETRIC_INTEGRITY_VALID_KEY
                .appendIdentifier(userId)
                .appendIdentifier(systemBioIntegrityState),
            value = value,
        )
    }
}
