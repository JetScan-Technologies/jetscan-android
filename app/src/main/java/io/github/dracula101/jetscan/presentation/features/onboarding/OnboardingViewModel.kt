package io.github.dracula101.jetscan.presentation.features.onboarding


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.platform.repository.config.ConfigRepository
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val ONBOARDING_STATE = ""

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val configRepository: ConfigRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<OnboardingState, Unit, OnboardingAction>(
    initialState = savedStateHandle[ONBOARDING_STATE] ?: OnboardingState(),
) {
    override fun handleAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.Ui.PageChange -> handlePageChange(action)
            is OnboardingAction.Ui.ExitOnboarding -> handleExitOnboarding(action.usePasswordlessSignIn)
        }
    }

    private fun handlePageChange(action: OnboardingAction.Ui.PageChange) {
        mutableStateFlow.update { it.copy(currentPage = action.page) }
    }

    private fun handleExitOnboarding(usePasswordlessSignIn: Boolean) {
        configRepository.isOnboardingCompleted = true
        if (usePasswordlessSignIn) {
            viewModelScope.launch {
                mutableStateFlow.update { it.copy(isLoadingAuthSignIn = true) }
                authRepository.loginPasswordLess()
                mutableStateFlow.update { it.copy(isLoadingAuthSignIn = false) }
            }
        }
    }
}


@Parcelize
data class OnboardingState(
    val currentPage: Int = 0,
    val isLoadingAuthSignIn: Boolean = false,
) : Parcelable

sealed class OnboardingAction {

    @Parcelize
    sealed class Ui : OnboardingAction(), Parcelable {

        data class PageChange(val page: Int) : Ui()

        data class ExitOnboarding(
            val usePasswordlessSignIn: Boolean,
        ) : Ui()

    }
}