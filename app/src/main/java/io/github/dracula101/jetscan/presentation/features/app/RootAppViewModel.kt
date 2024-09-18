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
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RootAppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
) : BaseViewModel<RootAppState, Unit, RootAppAction>(
    initialState = RootAppState.Splash,
) {

    init {
        authRepository
            .authStateFlow
            .onEach {
                Timber.i("Auth Change: $it")
                handleAction(RootAppAction.Internal.UserStateUpdateReceive(it))
            }
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                replay = 1,
            )
            .launchIn(viewModelScope)
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
        mutableStateFlow.update {
            when (action.userState) {
                null -> RootAppState.Auth
                else -> RootAppState.Home
            }
        }
    }

    private fun handleInitializeApp() {
        Firebase.initialize(context)
        val state = mutableStateFlow.value
        if (state == RootAppState.Splash) {
            mutableStateFlow.update {
                RootAppState.Auth
            }
        }
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
    data object Home : RootAppState()

    /**
     * App should show preview Document
     */
    @Parcelize
    data object PreviewDocument : RootAppState()

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
        ) : RootAppAction()

        /**
         * Initialize the app.
         */
        data object InitializeApp : RootAppAction()
    }
}
