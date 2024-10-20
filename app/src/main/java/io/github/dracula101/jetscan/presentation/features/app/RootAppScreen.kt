package io.github.dracula101.jetscan.presentation.features.app

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.github.dracula101.jetscan.presentation.features.auth.AUTH_GRAPH_ROUTE
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.features.auth.navigateToAuthGraph
import io.github.dracula101.jetscan.presentation.features.auth.register.REGISTER_ROUTE
import io.github.dracula101.jetscan.presentation.features.home.HOME_GRAPH_ROUTE
import io.github.dracula101.jetscan.presentation.features.home.homeGraph
import io.github.dracula101.jetscan.presentation.features.home.navigateToHomeGraph
import io.github.dracula101.jetscan.presentation.features.import_pdf.IMPORT_PDF_GRAPH_ROUTE
import io.github.dracula101.jetscan.presentation.features.import_pdf.importPdfDestination
import io.github.dracula101.jetscan.presentation.features.import_pdf.navigateToImportPdf
import io.github.dracula101.jetscan.presentation.features.onboarding.ONBOARDING_ROUTE
import io.github.dracula101.jetscan.presentation.features.onboarding.navigateToOnboarding
import io.github.dracula101.jetscan.presentation.features.onboarding.onboardingGraph
import io.github.dracula101.jetscan.presentation.features.splash.SPLASH_ROUTE
import io.github.dracula101.jetscan.presentation.features.splash.navigateToSplashRoute
import io.github.dracula101.jetscan.presentation.features.splash.splashDestination
import io.github.dracula101.jetscan.presentation.theme.NonNullEnterTransitionProvider
import io.github.dracula101.jetscan.presentation.theme.NonNullExitTransitionProvider
import io.github.dracula101.jetscan.presentation.theme.RootTransitionProviders
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicReference

@Composable
fun RootAppScreen(
    viewModel: RootAppViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    onNativeSplashRemove: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    val previousStateReference = remember { AtomicReference(state) }
    val isNotSplashScreen = state != RootAppState.Splash
    LaunchedEffect(isNotSplashScreen) {
        if (isNotSplashScreen) onNativeSplashRemove()
    }
    LaunchedEffect(Unit) {
        navController
            .currentBackStackEntryFlow
            .onEach {
                viewModel.trySendAction(RootAppAction.BackStackUpdate)
            }
            .launchIn(this)
        delay(500)
        viewModel.trySendAction(RootAppAction.Internal.InitializeApp)
    }

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
        enterTransition = { toEnterTransition()(this) },
        exitTransition = { toExitTransition()(this) },
        popEnterTransition = { toEnterTransition()(this) },
        popExitTransition = { toExitTransition()(this) },
    ) {
        splashDestination()
        onboardingGraph(navController)
        authGraph(navController)
        homeGraph(navController)
        importPdfDestination(navController)
    }
    val targetRoute = when (state) {
        RootAppState.Splash -> SPLASH_ROUTE
        RootAppState.Onboarding -> ONBOARDING_ROUTE
        RootAppState.Auth -> AUTH_GRAPH_ROUTE
        RootAppState.Home -> HOME_GRAPH_ROUTE
        is RootAppState.ImportPdf -> IMPORT_PDF_GRAPH_ROUTE
    }
    val currentRoute = navController.currentDestination?.rootLevelRoute()

    // Don't navigate if we are already at the correct root. This notably happens during process
    // death. In this case, the NavHost already restores state, so we don't have to navigate.
    // However, if the route is correct but the underlying state is different, we should still
    // proceed in order to get a fresh version of that route.
    if (currentRoute == targetRoute && previousStateReference.get() == state) {
        previousStateReference.set(state)
        return
    }
    previousStateReference.set(state)

    // In some scenarios on an emulator the Activity can leak when recreated
    // if we don't first clear focus anytime we change the root destination.
    (LocalContext.current as? Activity)?.currentFocus?.clearFocus()

    // When state changes, navigate to different root navigation state
    val rootNavOptions = navOptions {
        // When changing root navigation state, pop everything else off the back stack:
        popUpTo(navController.graph.id) {
            inclusive = false
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }

    // Use a LaunchedEffect to ensure we don't navigate too soon when the app first opens. This
    // avoids a bug that first appeared in Compose Material3 1.2.0-rc01 that causes the initial
    // transition to appear corrupted.
    LaunchedEffect(state) {
        when (val currentState = state) {
            RootAppState.Auth -> navController.navigateToAuthGraph(rootNavOptions)
            RootAppState.Onboarding -> navController.navigateToOnboarding()
            RootAppState.Splash -> navController.navigateToSplashRoute(rootNavOptions)
            RootAppState.Home -> navController.navigateToHomeGraph(rootNavOptions)
            is RootAppState.ImportPdf -> {
                navController.navigateToImportPdf(
                    tempPdfFile = currentState.importPdfEvent.tempPdfFile,
                    pdfName = currentState.importPdfEvent.pdfName,
                    navOptions = rootNavOptions
                )
            }
        }
    }
}

private fun NavDestination?.rootLevelRoute(): String? {
    if (this == null) {
        return null
    }
    if (parent?.route == null) {
        return route
    }
    return parent.rootLevelRoute()
}

/**
 * Define the enter transition for each route.
 */
@Suppress("MaxLineLength")
private fun AnimatedContentTransitionScope<NavBackStackEntry>.toEnterTransition(): NonNullEnterTransitionProvider =
    when (targetState.destination.rootLevelRoute()) {
        REGISTER_ROUTE -> RootTransitionProviders.Enter.slideUp
        IMPORT_PDF_GRAPH_ROUTE -> RootTransitionProviders.Enter.pushLeft
        else -> when (initialState.destination.rootLevelRoute()) {
            // Disable transitions when coming from the splash screen
            SPLASH_ROUTE -> RootTransitionProviders.Enter.none
            IMPORT_PDF_GRAPH_ROUTE -> RootTransitionProviders.Enter.stay
            else -> RootTransitionProviders.Enter.fadeIn
        }
    }

/**
 * Define the exit transition for each route.
 */
@Suppress("MaxLineLength")
private fun AnimatedContentTransitionScope<NavBackStackEntry>.toExitTransition(): NonNullExitTransitionProvider =
    when (initialState.destination.rootLevelRoute()) {
        // Disable transitions when coming from the splash screen
        SPLASH_ROUTE -> RootTransitionProviders.Exit.none
        REGISTER_ROUTE -> RootTransitionProviders.Exit.slideDown
        IMPORT_PDF_GRAPH_ROUTE -> RootTransitionProviders.Exit.pushRight
        else -> when (targetState.destination.rootLevelRoute()) {
            REGISTER_ROUTE -> RootTransitionProviders.Exit.stay
            IMPORT_PDF_GRAPH_ROUTE -> RootTransitionProviders.Exit.stay
            else -> RootTransitionProviders.Exit.fadeOut
        }
    }