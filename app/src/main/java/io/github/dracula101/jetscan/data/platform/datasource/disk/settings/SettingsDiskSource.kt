package io.github.dracula101.jetscan.data.platform.datasource.disk.settings

import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Primary access point for general settings-related disk information.
 */
@Suppress("TooManyFunctions")
interface SettingsDiskSource {

    /**
     * The theme of the application.
     */
    var appTheme: AppTheme

    /**
     * Flow of the theme of the application.
     */
    val appThemeFlow: Flow<AppTheme>

    /**
     * The system biometric integrity source.
     */
    var systemBiometricIntegritySource: String?

    /**
     * The last time the user logged in or created an account.
     */
    var hasUserLoggedInOrCreatedAccount: Boolean?

    /**
     * Flow of the last time the user logged in or created an account.
     */
    val hasUserLoggedInOrCreatedAccountFlow: Flow<Boolean?>

    /**
     * Clears all data
     */
    fun clearData()

    /**
     * Gets the account biometric integrity validity.
     */
    fun getAccountBiometricIntegrityValidity(
        userId: String,
        systemBioIntegrityState: String,
    ): Boolean?

    /**
     * Stores the account biometric integrity validity.
     */
    fun storeAccountBiometricIntegrityValidity(
        userId: String,
        systemBioIntegrityState: String,
        value: Boolean?,
    )
}