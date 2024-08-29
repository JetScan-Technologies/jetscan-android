package io.github.dracula101.jetscan.data.platform.repository

import io.github.dracula101.jetscan.data.platform.datasource.disk.SettingsDiskSource
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsRepositoryImpl(
    private val settingsDiskSource: SettingsDiskSource,
) : SettingsRepository {

    override var appTheme: AppTheme
        get() = settingsDiskSource.appTheme
        set(value) {
            settingsDiskSource.appTheme = value
        }

    override val appThemeStateFlow : StateFlow<AppTheme>
        get() = settingsDiskSource
            .appThemeFlow
            .stateIn(
                scope = CoroutineScope(Dispatchers.Unconfined),
                started = SharingStarted.Eagerly,
                initialValue = settingsDiskSource.appTheme,
            )

    override fun changeAppTheme(appTheme: AppTheme) {
        settingsDiskSource.appTheme = appTheme
    }

}