package io.github.dracula101.jetscan.presentation.features.auth.register


import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.model.RegisterResult
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.platform.utils.Validators.isValidEmail
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.DialogState
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

private const val KEY_STATE = "state"


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : BaseViewModel<RegisterState, Unit, RegisterAction>(
    initialState = savedStateHandle[KEY_STATE] ?: RegisterState(),
) {

    override fun handleAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.RegisterWith.EmailAndPassword -> handleLoginWithEmailPass()
            is RegisterAction.Ui.NameFieldChanged -> handleNameFieldChanged(action.name)
            is RegisterAction.Ui.EmailFieldChanged -> handleEmailFieldChanged(action.email)
            is RegisterAction.Ui.PasswordFieldChanged -> handlePasswordFieldChanged(action.password)
            is RegisterAction.Ui.PasswordVisibilityChanged -> handlePasswordVisibilityChanged()
            is RegisterAction.Ui.TermsAndConditionsChanged -> handleTermsAndConditionsChanged()
            is RegisterAction.Ui.DismissDialog -> handleDismissDialog()
            is RegisterAction.Ui.DismissSnackbar -> handleDismissSnackbar()
        }
    }

    private fun handleLoginWithEmailPass() {
        viewModelScope.launch {
            try{
                if(checkForErrors()) return@launch
                // All checks passed, proceed with login
                mutableStateFlow.update { state.copy( isLoading = true ) }
                val authResult = authRepository.register(
                    name = state.nameInputField.value,
                    email = state.emailInputField.value,
                    password = state.passwordInputField.value,
                )
                if (authResult is RegisterResult.Error){
                    mutableStateFlow.update {
                        state.copy(
                            isLoading = false,
                            snackbarState = SnackbarState.ShowError(
                                title = "Register Error",
                                message = authResult.errorMessage ?: "An unexpected error occurred",
                                errorCode = authResult.errorCode
                            )
                        )
                    }
                }
            } catch (e: CancellationException){
                Timber.e(e)
                mutableStateFlow.update {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState.ShowError(
                            title = "Login Error",
                            message = e.localizedMessage ?: "An unexpected error occurred",
                        )
                    )
                }
            }
        }
    }

    private fun checkForErrors() : Boolean {
        val isNameValid = state.nameInputField.value.isNotEmpty()
        val isEmailValid = state.emailInputField.value.isValidEmail()
        val isPasswordValid = state.passwordInputField.value.isNotEmpty()
        val isTermsAndConditionsChecked = state.termsAndConditionsCheckbox.isChecked
        Timber.d("Valid-> Name: $isNameValid, Email: $isEmailValid, Password: $isPasswordValid, T&C: $isTermsAndConditionsChecked")
        mutableStateFlow.update {
            state.copy(
                nameInputField = state.nameInputField.copy(
                    errorText = if (isNameValid) null else "Invalid name"
                ),
                emailInputField = state.emailInputField.copy(
                    errorText = if (isEmailValid) null else "Invalid email"
                ),
                passwordInputField = state.passwordInputField.copy(
                    errorText = if (isPasswordValid) null else "Invalid password"
                ),
                termsAndConditionsCheckbox = state.termsAndConditionsCheckbox.copy(
                    isChecked = isTermsAndConditionsChecked
                ),
            )
        }
        val isInputFieldValid = isNameValid && isEmailValid && isPasswordValid
        if ((isInputFieldValid) && !isTermsAndConditionsChecked){
            mutableStateFlow.update {
                state.copy(
                    snackbarState = SnackbarState.ShowWarning(
                        title = "Terms and Conditions",
                        message = "Please accept the terms and conditions to proceed"
                    )
                )
            }
        }
        return !(isInputFieldValid && isTermsAndConditionsChecked)
    }

    private fun handleNameFieldChanged(updatedName: String) {
        mutableStateFlow.update {
            state.copy(
                nameInputField = state.nameInputField.copy(
                    value = updatedName,
                    errorText = if (state.nameInputField.errorText != null) {
                        if (updatedName.isNotEmpty()) null else "Invalid name"
                    } else null
                )
            )
        }
    }

    private fun handleEmailFieldChanged(updatedEmail: String) {
        mutableStateFlow.update {
            state.copy(
                emailInputField = state.emailInputField.copy(
                    value = updatedEmail,
                    errorText = if (state.emailInputField.errorText != null) {
                        if (updatedEmail.isValidEmail()) null else "Invalid email"
                    } else null
                )
            )
        }
    }

    private fun handlePasswordFieldChanged(updatedPassword: String) {
        mutableStateFlow.update {
            state.copy(
                passwordInputField = state.passwordInputField.copy(
                    value = updatedPassword,
                    errorText = if (state.passwordInputField.errorText != null) {
                        if (updatedPassword.isNotEmpty()) null else "Invalid password"
                    } else null
                )
            )
        }
    }

    private fun handlePasswordVisibilityChanged() {
        mutableStateFlow.update {
            state.copy(
                passwordInputField = state.passwordInputField.copy(
                    isPasswordVisible = !state.passwordInputField.isPasswordVisible,
                )
            )
        }
    }

    private fun handleTermsAndConditionsChanged() {
        val checkChangeState = state.termsAndConditionsCheckbox.isChecked.not()
        mutableStateFlow.update {
            state.copy(
                termsAndConditionsCheckbox = state.termsAndConditionsCheckbox.copy(
                    isChecked = checkChangeState,
                )
            )
        }
    }

    private fun handleDismissDialog() {
        mutableStateFlow.update {
            state.copy(
                dialogState = null,
                isLoading = false,
            )
        }
    }

    private fun handleDismissSnackbar(){
        mutableStateFlow.update {
            state.copy(
                snackbarState = null,
                isLoading = false,
            )
        }
    }

    override fun onCleared() {
        savedStateHandle[KEY_STATE] = state
        super.onCleared()
    }
}

/**
 * Models the state of the login screen.
 */
@Parcelize
data class RegisterState(
    val nameInputField: InputField.Name = InputField.Name(),
    val emailInputField: InputField.Email = InputField.Email(),
    val passwordInputField: InputField.Password = InputField.Password(),
    val termsAndConditionsCheckbox: Checkbox.TermsAndConditions = Checkbox.TermsAndConditions(),
    val dialogState: DialogState? = null,
    val snackbarState: SnackbarState? = null,
    val isLoading : Boolean = false,

    ) : Parcelable {

    sealed class InputField : Parcelable {

        @Parcelize
        data class Name(
            val value: String = "",
            val errorText: String? = null,
        ) : InputField()

        @Parcelize
        data class Email(
            val value: String = "",
            val errorText: String? = null,
        ) : InputField()

        @Parcelize
        data class Password(
            @IgnoredOnParcel
            val value: String = "",
            val errorText: String? = null,
            val isPasswordVisible: Boolean = false,
        ) : InputField()
    }

    sealed class Checkbox : Parcelable {

        @Parcelize
        data class TermsAndConditions(
            val isChecked: Boolean = false,
        ) : Checkbox()

    }
}

sealed class RegisterAction {

    sealed class RegisterWith : RegisterAction() {
        data object EmailAndPassword : RegisterWith()
    }

    sealed class Ui : RegisterAction() {
        data class NameFieldChanged(
            val name: String,
        ) : Ui()
        data class EmailFieldChanged(
            val email: String,
        ) : Ui()
        data class PasswordFieldChanged(
            val password: String,
        ) : Ui()
        data object PasswordVisibilityChanged : Ui()
        data object TermsAndConditionsChanged : Ui()
        data object DismissDialog : Ui()
        data object DismissSnackbar : Ui()
    }

}