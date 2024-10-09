package io.github.dracula101.jetscan

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.platform.manager.opencv.OpenCvManager
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * A view model that helps launch actions for the [MainActivity].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val opencvManager: OpenCvManager,
) : BaseViewModel<MainState, MainEvent, MainAction>(
    initialState = MainState(
        theme = AppTheme.DARK,
    ),
) {

    init {
        opencvManager.initialize()
        settingsRepository.appThemeStateFlow
            .map { MainAction.Internal.ThemeUpdate(it) }
            .onEach { sendAction(it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MainAction) {
        when (action) {
            is MainAction.Internal.CurrentUserStateChange -> {
                handleCurrentUserStateChange()
            }
            is MainAction.Internal.ThemeUpdate -> {
                handleAppThemeUpdated(action)
            }
            is MainAction.ReceiveFirstIntent -> {
                handleFirstIntentReceived(action)
            }
            is MainAction.ReceiveNewIntent -> {
                handleNewIntentReceived(action)
            }
            else -> {}
        }
    }

    private fun handleCurrentUserStateChange() {
        recreateUiAndGarbageCollect()
    }

    private fun handleAppThemeUpdated(action: MainAction.Internal.ThemeUpdate) {
        mutableStateFlow.update { it.copy(theme = action.theme) }
        settingsRepository.appTheme = action.theme
    }

    private fun handleFirstIntentReceived(action: MainAction.ReceiveFirstIntent) {
        handleIntent(
            intent = action.intent,
            isFirstIntent = true,
        )
    }

    private fun handleNewIntentReceived(action: MainAction.ReceiveNewIntent) {
        handleIntent(
            intent = action.intent,
            isFirstIntent = false,
        )
    }

    private fun handleIntent(
        intent: Intent,
        isFirstIntent: Boolean,
    ) {
    }

    private fun recreateUiAndGarbageCollect() {
        sendEvent(MainEvent.Recreate)
    }
}

/**
 * Models state for the [MainActivity].
 */
@Parcelize
data class MainState(
    val theme: AppTheme,
) : Parcelable
/**
 * Models actions for the [MainActivity].
 */
sealed class MainAction {

    /**
     * Receive first Intent by the application.
     */
    data class ReceiveFirstIntent(val intent: Intent) : MainAction()

    /**
     * Receive Intent by the application.
     */
    data class ReceiveNewIntent(val intent: Intent) : MainAction()

    /**
     * Actions for internal use by the ViewModel.
     */
    sealed class Internal : MainAction() {

        /**
         * Indicates a relevant change in the current user state.
         */
        data object CurrentUserStateChange : Internal()
        /**
         * Indicates that the app theme has changed.
         */
        data class ThemeUpdate(
            val theme: AppTheme,
        ) : Internal()
    }
}

/**
 * Represents events that are emitted by the [MainViewModel].
 */
sealed class MainEvent {
    /**
     * Event indicating that the UI should recreate itself.
     */
    data object Recreate : MainEvent()
}
