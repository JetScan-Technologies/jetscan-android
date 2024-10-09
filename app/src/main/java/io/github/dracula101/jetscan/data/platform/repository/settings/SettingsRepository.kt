package io.github.dracula101.jetscan.data.platform.repository.settings

import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {

    /**
     * The currently stored [AppTheme].
     */
    var appTheme: AppTheme

    /**
     * Tracks changes to the [AppTheme].
     */
    val appThemeStateFlow: StateFlow<AppTheme>

    fun changeAppTheme(appTheme: AppTheme)

}