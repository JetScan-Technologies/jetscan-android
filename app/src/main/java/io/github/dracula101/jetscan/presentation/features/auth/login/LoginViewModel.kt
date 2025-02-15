package io.github.dracula101.jetscan.presentation.features.auth.login

import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.data.auth.model.GoogleSignInResult
import io.github.dracula101.jetscan.data.auth.model.LoginResult
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
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : BaseViewModel<LoginState, Unit, LoginAction>(
    initialState = savedStateHandle[KEY_STATE] ?: LoginState(),
) {

    // Get Google Sign In Client
    val googleSignInClient = authRepository.getGoogleSignInClient()

    override fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.LoginWith.EmailAndPassword -> handleLoginWithEmailPass()
            is LoginAction.Intent.GoogleSignInIntent -> handleGoogleSignInIntent(action.intent, action.isFromOneTapClient)
            is LoginAction.Intent.GoogleSignInCancelled -> handleGoogleSignInCancelled()
            is LoginAction.Intent.GoogleSignInFailed -> handleGoogleSignInFailed(action.errorMessage, action.errorCode)
            is LoginAction.Ui.EmailFieldChanged -> handleEmailFieldChanged(action.email)
            is LoginAction.Ui.PasswordFieldChanged -> handlePasswordFieldChanged(action.password)
            is LoginAction.Ui.PasswordVisibilityChanged -> handlePasswordVisibilityChanged()
            is LoginAction.Ui.TermsAndConditionsChanged -> handleTermsAndConditionsChanged()
            is LoginAction.Ui.DismissDialog -> handleDismissDialog()
            is LoginAction.Ui.DismissSnackbar -> handleDismissSnackbar()
            is LoginAction.Ui.GoogleSignInClicked -> handleGoogleSignInClick()
            is LoginAction.Ui.SkipLogin -> handleSkipLogin()
            is LoginAction.Ui.Logout -> handleLogout()
        }
    }

    suspend fun getOneTapClientIntent(): IntentSenderRequest? {
        return try{
            val intentSender = authRepository.getGoogleSignInIntentOneTapClient()
            if (intentSender != null) {
                IntentSenderRequest.Builder(intentSender).build()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }



    private fun handleLoginWithEmailPass() {
        viewModelScope.launch {
            try {
                if (checkForErrors()) return@launch
                mutableStateFlow.update { state.copy(isLoading = true) }
                val authResult = authRepository.login(
                    email = state.emailInputField.value,
                    password = state.passwordInputField.value
                )
                if (authResult is LoginResult.Error){
                    mutableStateFlow.update {
                        state.copy(
                            isLoading = false,
                            snackbarState = SnackbarState.ShowError(
                                title = "Login Error",
                                message = authResult.errorMessage ?: "An error occurred while logging in",
                                errorCode = authResult.errorCode
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                mutableStateFlow.update {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState.ShowError(
                            title = "Login Error",
                            message = "An error occurred while logging in",
                            errorCode = "UNKNOWN_ERROR"
                        )
                    )
                }
            }
        }
    }

    private fun  handleGoogleSignInClick(){
        mutableStateFlow.update { state.copy(isLoading=true) }
        /*
        * Activity Result Launcher for Google Sign In will be called here frm UI side
        * */
    }

    private fun handleSkipLogin(){
        viewModelScope.launch {
            authRepository.guestLogin()
        }
    }

    private fun handleGoogleSignInIntent(
        intent: Intent,
        isFromOneTapClient: Boolean
    ) {
        try{
            viewModelScope.launch {
                val authResult = if (isFromOneTapClient) {
                    authRepository.signInWithGoogleOneTapClient(intent)
                } else {
                    authRepository.signInWithGooglePlayServices(intent)
                }
                if (authResult is GoogleSignInResult.Success) {
                    mutableStateFlow.update {
                        state.copy(
                            isLoading = false,
                            snackbarState = SnackbarState.ShowSuccess(
                                title = "Google Sign-in Success"
                            )
                        )
                    }
                } else if (authResult is GoogleSignInResult.Error){
                    mutableStateFlow.update {
                        state.copy(
                            isLoading = false,
                            snackbarState = SnackbarState.ShowError(
                                title = "Google Sign-in Error",
                                message = authResult.errorMessage ?: "",
                                errorCode = authResult.errorCode
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            mutableStateFlow.update {
                val errorMessage = if (e is CancellationException) {
                    "An error occurred while signing in with Google"
                } else {
                    "Couldn't sign in with Google Login"
                }
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState.ShowError(
                        title = "Google Sign-in Error",
                        message = errorMessage,
                        errorCode = "UNKNOWN_ERROR"
                    )
                )
            }
        }
    }

    private fun handleGoogleSignInCancelled() {
        mutableStateFlow.update {
            state.copy(
                isLoading = false,
                dialogState = DialogState.ShowError(
                    title = "Google Sign-in Cancelled",
                    message = "Google Sign-in was cancelled"
                )
            )
        }
    }

    private fun handleGoogleSignInFailed(
        errorMessage: String,
        errorCode: String
    ) {
        mutableStateFlow.update {
            state.copy(
                isLoading = false,
                snackbarState = SnackbarState.ShowError(
                    title = "Google Sign-in Failed",
                    message = errorMessage,
                    errorCode = errorCode
                )
            )
        }
    }

    private fun checkForErrors() : Boolean {
        val isEmailValid = state.emailInputField.value.isValidEmail()
        val isPasswordValid = state.passwordInputField.value.isNotEmpty()
        val isTermsAndConditionsChecked = state.termsAndConditionsCheckbox.isChecked
        Timber.d("Valid-> Email: $isEmailValid, Password: $isPasswordValid, T&C: $isTermsAndConditionsChecked")
        mutableStateFlow.update {
            state.copy(
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
        if (isEmailValid && isPasswordValid && !isTermsAndConditionsChecked){
            mutableStateFlow.update {
                state.copy(
                    snackbarState = SnackbarState.ShowWarning(
                        title = "Terms and Conditions",
                        message = "Please accept the terms and conditions to proceed"
                    )
                )
            }
        }
        return !(isEmailValid && isPasswordValid && isTermsAndConditionsChecked)
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

    private fun handleLogout(){
        viewModelScope.launch {
            authRepository.logout()
            mutableStateFlow.update {
                state.copy(
                    snackbarState = SnackbarState.ShowSuccess(
                        title = "Logout successful"
                    )
                )
            }
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
data class LoginState(
    val emailInputField: InputField.Email = InputField.Email(),
    val passwordInputField: InputField.Password = InputField.Password(),
    val termsAndConditionsCheckbox: Checkbox.TermsAndConditions = Checkbox.TermsAndConditions(),
    val dialogState: DialogState? = null,
    val snackbarState: SnackbarState? = null,
    val isLoading : Boolean = false,
) : Parcelable {

    sealed class InputField : Parcelable {
        @Parcelize
        data class Email(
            val value: String = "",
            val errorText: String? = null,
        ) : InputField()

        @Parcelize
        data class Password(
            @IgnoredOnParcel
            val value: String = "",
            val isPasswordVisible: Boolean = false,
            val errorText: String? = null,
        ) : InputField()
    }

    sealed class Checkbox : Parcelable {

        @Parcelize
        data class TermsAndConditions(
            val isChecked: Boolean = false,
        ) : Checkbox()

    }
}

sealed class LoginAction {

    sealed class LoginWith : LoginAction() {
        data object EmailAndPassword : LoginWith()
    }

    sealed class Ui : LoginAction() {
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
        data object GoogleSignInClicked : Ui()
        data object SkipLogin: Ui()
        data object Logout: Ui()
    }

    sealed class Intent : LoginAction() {
        data class GoogleSignInIntent(
            val intent: android.content.Intent,
            val isFromOneTapClient: Boolean
        ) : Intent()
        data object GoogleSignInCancelled: Intent()
        data class GoogleSignInFailed(
            val errorMessage: String,
            val errorCode: String
        ): Intent()
    }

}