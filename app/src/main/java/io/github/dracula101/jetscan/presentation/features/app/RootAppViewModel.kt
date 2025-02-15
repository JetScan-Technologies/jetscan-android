package io.github.dracula101.jetscan.presentation.features.app

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dracula101.jetscan.data.auth.model.UserState
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.platform.manager.models.SpecialCircumstance
import io.github.dracula101.jetscan.data.platform.manager.special_circumstance.SpecialCircumstanceManager
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.data.platform.utils.zip
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RootAppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
    private val specialCircumstanceManager: SpecialCircumstanceManager,
) : BaseViewModel<RootAppState, Unit, RootAppAction>(
    initialState = RootAppState.Splash,
) {

    init {
        combine(
            authRepository.authStateFlow,
            specialCircumstanceManager.specialCircumstanceFlow,
        ){ authUser, specialCircumstance ->
            RootAppAction.Internal.UserStateUpdateReceive(authUser, specialCircumstance)
        }.onEach {
            trySendAction(it)
        }.launchIn(viewModelScope)
    }

    override fun handleAction(action: RootAppAction) {
        when (action) {
            is RootAppAction.BackStackUpdate -> handleBackStackUpdate()
            is RootAppAction.Internal.InitializeApp -> handleInitializeApp()
            is RootAppAction.Internal.UserStateUpdateReceive -> handleUserStateUpdateReceive(action)
        }
    }

    private fun handleBackStackUpdate() {
    }

    private fun handleUserStateUpdateReceive(
        action: RootAppAction.Internal.UserStateUpdateReceive,
    ) {
        // Handle all navigation from here
        mutableStateFlow.update {
            when(action.userState)  {
                null -> {
                    if (!configRepository.isOnboardingCompleted) {
                        RootAppState.Onboarding
                    } else {
                        RootAppState.Auth
                    }
                }
                else -> {
                    action.specialCircumstance?.let { specialCircumstance ->
                        when(specialCircumstance) {
                            is SpecialCircumstance.ImportPdfEvent -> {
                                RootAppState.ImportPdf(specialCircumstance)
                            }
                        }
                    } ?: RootAppState.Home(isAnonymous = action.userState.isGuest)
                }
            }
        }
    }

    private fun handleInitializeApp() {
        Firebase.initialize(context)
    }
}

/**
 * Models root level destinations for the app.
 */
sealed class RootAppState : Parcelable {

    /**
     * App should show auth nav graph.
     */
    @Parcelize
    data object Auth : RootAppState()

    /**
     * App should show splash nav graph.
     */
    @Parcelize
    data object Splash : RootAppState()

    /**
     * App should show home.
     */
    @Parcelize
    data class Home(
        val isAnonymous: Boolean = false
    ) : RootAppState()

    /**
     * App should show import pdf.
     */
    @Parcelize
    data class ImportPdf(
        val importPdfEvent: SpecialCircumstance.ImportPdfEvent
    ) : RootAppState()

    /**
     * App should show onboarding
     */
    @Parcelize
    data object Onboarding : RootAppState()

}

/**
 * Models root level navigation actions.
 */
sealed class RootAppAction {
    /**
     * Indicates the backstack has changed.
     */
    data object BackStackUpdate : RootAppAction()

    /**
     * Internal ViewModel actions.
     */
    sealed class Internal {

        /**
         * User state in the repository layer changed.
         */
        data class UserStateUpdateReceive(
            val userState: UserState?,
            val specialCircumstance: SpecialCircumstance?,
        ) : RootAppAction()

        /**
         * Initialize the app.
         */
        data object InitializeApp : RootAppAction()
    }
}
