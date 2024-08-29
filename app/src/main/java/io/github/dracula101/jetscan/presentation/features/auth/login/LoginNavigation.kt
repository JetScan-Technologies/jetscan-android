package io.github.dracula101.jetscan.presentation.features.auth.login

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithRootPushTransitions
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val LOGIN_ROUTE = "login"

fun NavGraphBuilder.createLoginDestination(
    onNavigateToRegister: () -> Unit,
) {
    composableWithStayTransitions(
        route = LOGIN_ROUTE,
    ){
        LockOrientation(
            isPortraitOnly = true
        ){
            LoginScreen(
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }
}

fun NavHostController.navigateToLoginRoute() {
    navigate(LOGIN_ROUTE)
}