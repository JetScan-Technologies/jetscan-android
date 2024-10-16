package io.github.dracula101.jetscan.presentation.features.home.settings_view

import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.model.UserState
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.ocr.repository.OcrRepository
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject


const val SETTINGS_STATE = ""

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val contentResolver: ContentResolver,
) : BaseViewModel<SettingsState, Unit, SettingsAction>(
    initialState = savedStateHandle[SETTINGS_STATE] ?: SettingsState(),
) {

    init {
        authRepository
            .authStateFlow
            .onEach { user ->
                mutableStateFlow.update { it.copy(user = user) }
            }
            .launchIn(viewModelScope)
        settingsRepository
            .appThemeStateFlow
            .onEach { appTheme ->
                mutableStateFlow.update {
                    it.copy(
                        isDarkTheme = when (appTheme) {
                            AppTheme.LIGHT -> false
                            AppTheme.DARK -> true
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.Ui.ChangeTheme -> handleThemeChange()
            is SettingsAction.Ui.Logout -> handleLogout()
            is SettingsAction.Alerts.ShowLogoutConfirmation -> handleShowLogoutConfirmation()
            is SettingsAction.Alerts.DismissAlert -> handleDismissAlert(
                bottomSheet = action.bottomSheet,
                dialog = action.dialog
            )
        }
    }

    private fun handleShowLogoutConfirmation() {
        mutableStateFlow.update {
            it.copy(bottomSheetState = SettingsBottomSheetState.ShowLogout)
        }
    }

    private fun handleThemeChange() {
        val newTheme = when (settingsRepository.appTheme) {
            AppTheme.LIGHT -> AppTheme.DARK
            AppTheme.DARK -> AppTheme.LIGHT
        }
        settingsRepository.changeAppTheme(newTheme)
    }

    private fun handleLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun handleDismissAlert(bottomSheet: Boolean = false, dialog: Boolean = false) {
        mutableStateFlow.update {
            it.copy(bottomSheetState = if (bottomSheet) null else it.bottomSheetState)
        }
    }
}


@Parcelize
data class SettingsState(
    val user: UserState? = null,
    val isDarkTheme: Boolean = false,
    val dialogState: DialogState? = null,
    val bottomSheetState: SettingsBottomSheetState? = null,
) : Parcelable {

    sealed class DialogState : Parcelable {}

}

sealed class SettingsAction {

    @Parcelize
    sealed class Ui : SettingsAction(), Parcelable {

        data object ChangeTheme : Ui(), Parcelable
        data object Logout: Ui(), Parcelable
    }

    @Parcelize
    sealed class Alerts : SettingsAction(), Parcelable {
        data object ShowLogoutConfirmation : Alerts(), Parcelable
        data class DismissAlert(
            val bottomSheet: Boolean = false,
            val dialog: Boolean = false,
        ) : Alerts(), Parcelable
    }
}

sealed class SettingsBottomSheetState : Parcelable {
    @Parcelize
    data object ShowLogout : SettingsBottomSheetState()
}
