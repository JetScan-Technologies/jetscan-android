package io.github.dracula101.jetscan.presentation.features.auth.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
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