package io.github.dracula101.jetscan

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.dracula101.jetscan.presentation.features.app.RootAppScreen
import io.github.dracula101.jetscan.presentation.platform.composition.LocalManagerProvider
import io.github.dracula101.jetscan.presentation.platform.feature.setting.model.AppTheme
import io.github.dracula101.jetscan.presentation.theme.JetScanAppTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        var shouldShowSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition { shouldShowSplashScreen }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by mainViewModel.stateFlow.collectAsStateWithLifecycle()
            LocalManagerProvider {
                LaunchedEffect(Unit) {
                    mainViewModel.trySendAction(MainAction.Internal.ThemeUpdate(
                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            UI_MODE_NIGHT_YES -> AppTheme.DARK
                            else -> AppTheme.LIGHT
                        }
                    ))
                }
                JetScanAppTheme(
                    darkTheme = when(state.theme){
                        AppTheme.LIGHT -> false
                        AppTheme.DARK -> true
                    }
                ) {
                    RootAppScreen(
                        onNativeSplashRemove = {
                            shouldShowSplashScreen = false
                        }
                    )
                }
            }
        }
    }
}