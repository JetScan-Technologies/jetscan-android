package io.github.dracula101.jetscan.presentation.features.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.dracula101.jetscan.presentation.features.auth.login.LOGIN_ROUTE
import io.github.dracula101.jetscan.presentation.features.auth.login.createLoginDestination
import io.github.dracula101.jetscan.presentation.features.auth.register.createRegisterDestination
import io.github.dracula101.jetscan.presentation.features.auth.register.navigateToRegisterRoute

const val AUTH_GRAPH_ROUTE = "auth_graph"


@Suppress("LongMethod")
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = LOGIN_ROUTE,
        route = AUTH_GRAPH_ROUTE,
    ) {
        createLoginDestination(
            onNavigateToRegister = {
                navController.navigateToRegisterRoute()
            }
        )
        createRegisterDestination(
            onNavigateToLogin = {
                navController.navigateUp()
            }
        )
    }
}

/**
 * Navigate to the auth screen. Note this will only work if auth destination was added
 * via [authGraph].
 */
fun NavController.navigateToAuthGraph(
    navOptions: NavOptions? = null,
) {
    navigate(AUTH_GRAPH_ROUTE, navOptions)
}
