package io.github.dracula101.jetscan.presentation.features.auth.register

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithSlideTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val REGISTER_ROUTE = "register"


fun NavGraphBuilder.createRegisterDestination(
    onNavigateToLogin: () -> Unit,
) {
    composableWithSlideTransitions(
        route = REGISTER_ROUTE,
    ){
        LockOrientation(
            isPortraitOnly = true
        ){
            RegisterScreen(
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}

fun NavHostController.navigateToRegisterRoute() {
    navigate(REGISTER_ROUTE)
}