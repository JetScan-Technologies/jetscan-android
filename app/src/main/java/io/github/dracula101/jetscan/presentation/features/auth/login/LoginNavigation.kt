package io.github.dracula101.jetscan.presentation.features.auth.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithStayTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation

const val LOGIN_ROUTE = "login"
const val LOGIN_FROM_HOME_ARG = "from_home"

const val LOGIN_START_ROUTE = "$LOGIN_ROUTE?$LOGIN_FROM_HOME_ARG={$LOGIN_FROM_HOME_ARG}"

fun NavGraphBuilder.createLoginDestination(
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composableWithStayTransitions(
        route = LOGIN_START_ROUTE,
        arguments = listOf(
            navArgument(LOGIN_FROM_HOME_ARG) {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ){
        LockOrientation(
            isPortraitOnly = true
        ){
            val isRequestFromHome = it.arguments?.getBoolean(LOGIN_FROM_HOME_ARG) ?: false
            LoginScreen(
                onNavigateToRegister = onNavigateToRegister,
                onNavigateBack = onNavigateBack,
                isRequestFromHome = isRequestFromHome
            )
        }
    }
}

fun NavHostController.navigateToLoginRoute(
    fromHome: Boolean = false
) {
    navigate("$LOGIN_ROUTE?$LOGIN_FROM_HOME_ARG=$fromHome")
}