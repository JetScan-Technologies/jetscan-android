package io.github.dracula101.jetscan

import android.content.ContentResolver
import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.document.manager.DocumentManager
import io.github.dracula101.jetscan.data.document.models.MimeType
import io.github.dracula101.jetscan.data.platform.manager.models.SpecialCircumstance
import io.github.dracula101.jetscan.data.platform.manager.special_circumstance.SpecialCircumstanceManager
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.data.platform.repository.settings.SettingsRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

private const val SPECIAL_CIRCUMSTANCE_KEY = "special-circumstance"

/**
 * A view model that helps launch actions for the [MainActivity].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val configRepository: ConfigRepository,
    private val specialCircumstanceManager: SpecialCircumstanceManager,
    private val documentManager: DocumentManager,
    private val contentResolver: ContentResolver,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<MainState, MainEvent, MainAction>(
    initialState = MainState(
        theme = AppTheme.DARK,
    ),
) {
        private var specialCircumstance: SpecialCircumstance?
        get() = savedStateHandle[SPECIAL_CIRCUMSTANCE_KEY]
        set(value) {
            savedStateHandle[SPECIAL_CIRCUMSTANCE_KEY] = value
        }

    init {
        specialCircumstanceManager.specialCircumstance = specialCircumstance

        specialCircumstanceManager
            .specialCircumstanceFlow
            .onEach { specialCircumstance = it }
            .launchIn(viewModelScope)

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
        }
    }

    private fun handleCurrentUserStateChange() {
        recreateUiAndGarbageCollect()
    }

    private fun handleAppThemeUpdated(action: MainAction.Internal.ThemeUpdate) {
        mutableStateFlow.update { it.copy(theme = action.theme) }
        settingsRepository.appTheme = action.theme
    }

    private fun handleFirstIntentReceived(action: MainAction.ReceiveFirstIntent) = handleIntent(intent = action.intent, isFirstIntent = true)

    private fun handleNewIntentReceived(action: MainAction.ReceiveNewIntent) = handleIntent(intent = action.intent, isFirstIntent = false)

    private fun handleIntent(
        intent: Intent,
        isFirstIntent: Boolean,
    ) {
        val intentType = intent.type
        val intentAction = intent.action
        val intentData = intent.data
        Timber.i("Intent received: \nType: $intentType\nAction: $intentAction\nData: $intentData, \nType: ${intentData?.javaClass?.simpleName}")
        when {
            intentAction == Intent.ACTION_VIEW && intentType == MimeType.APPLICATION_PDF.value -> {
                val pdfUri = intentData ?: return
                val uriName = documentManager.getFileName(pdfUri)
                specialCircumstanceManager.specialCircumstance = SpecialCircumstance.ImportPdfEvent(
                    pdfName = uriName ?: "",
                    uri = pdfUri
                )
            }
            else -> {}
        }
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
