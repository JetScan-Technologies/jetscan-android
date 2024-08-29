package io.github.dracula101.jetscan.presentation.features.auth.register

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.component.button.FlatButton
import io.github.dracula101.jetscan.presentation.platform.component.button.GradientButton
import io.github.dracula101.jetscan.presentation.platform.component.button.NoRippleButton
import io.github.dracula101.jetscan.presentation.platform.component.checkbox.CircleCheckbox
import io.github.dracula101.jetscan.presentation.platform.component.extensions.gradientContainer
import io.github.dracula101.jetscan.presentation.platform.component.scaffold.JetScanScaffold
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showErrorSnackBar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showSuccessSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.snackbar.util.showWarningSnackbar
import io.github.dracula101.jetscan.presentation.platform.component.textfield.AppTextField
import io.github.dracula101.jetscan.presentation.platform.feature.app.model.SnackbarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    state.value.snackbarState?.let {
        RegisterAlerts(
            snackbarState = it,
            snackbarHostState = snackbarHostState,
            dismissSnackbar = {
                viewModel.trySendAction(RegisterAction.Ui.DismissSnackbar)
            }
        )
    }
    JetScanScaffold(
        snackbarHostState = snackbarHostState
    ) { padding, windowSize ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.register_illustration),
                        contentDescription = "register_illustration",
                        modifier = Modifier.fillMaxSize(0.7f),
                    )
                }
            }
            item {
                Text(
                    "Register",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Create an account to sign in back",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = state.value.nameInputField.value,
                    onValueChange = {
                        viewModel.trySendAction(RegisterAction.Ui.NameFieldChanged(it))
                    },
                    label = "Name",
                    isError = state.value.nameInputField.errorText != null,
                    errorText = state.value.nameInputField.errorText,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = state.value.emailInputField.value,
                    onValueChange = {
                        viewModel.trySendAction(RegisterAction.Ui.EmailFieldChanged(it))
                    },
                    label = "Email",
                    isError = state.value.emailInputField.errorText != null,
                    errorText = state.value.emailInputField.errorText,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = state.value.passwordInputField.value,
                    onValueChange = {
                        viewModel.trySendAction(RegisterAction.Ui.PasswordFieldChanged(it))
                    },
                    label = "Password",
                    isError = state.value.passwordInputField.errorText != null,
                    errorText = state.value.passwordInputField.errorText,
                    visualTransformation = if (state.value.passwordInputField.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.trySendAction(RegisterAction.Ui.PasswordVisibilityChanged)
                            }
                        ) {
                            Icon(
                                imageVector = if (!state.value.passwordInputField.isPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = "Hide password",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleCheckbox(
                        selected = state.value.termsAndConditionsCheckbox.isChecked,
                        onChecked = {
                            viewModel.trySendAction(RegisterAction.Ui.TermsAndConditionsChanged)
                        }
                    )
                    Text(
                        "I agree to the terms and conditions",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    viewModel.trySendAction(RegisterAction.Ui.TermsAndConditionsChanged)
                                }
                            )
                    )
                }

                GradientButton(
                    onClick = {
                        viewModel.trySendAction(RegisterAction.RegisterWith.EmailAndPassword)
                    },
                    showContent = state.value.isLoading,
                    loadingContent = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 1.5.dp
                        )
                    },
                    text = "Register",
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                NoRippleButton( onClick = onNavigateToLogin ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Login",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LoginOption(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.05f), shape)
            .border(width = 1.dp, color = color.copy(alpha = 0.3f), shape = shape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun RegisterAlerts(
    snackbarState: SnackbarState,
    snackbarHostState: SnackbarHostState,
    dismissSnackbar: () -> Unit = {},
) {
    LaunchedEffect(snackbarState) {
        when (snackbarState) {
            is SnackbarState.ShowSuccess -> {
                snackbarHostState.showSuccessSnackbar(
                    message = snackbarState.title,
                    onDismiss = dismissSnackbar
                )
            }

            is SnackbarState.ShowWarning -> {
                snackbarHostState.showWarningSnackbar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = dismissSnackbar
                )
            }

            is SnackbarState.ShowError -> {
                snackbarHostState.showErrorSnackBar(
                    message = snackbarState.title,
                    detail = snackbarState.message,
                    onDismiss = dismissSnackbar
                )
            }
        }
    }
}