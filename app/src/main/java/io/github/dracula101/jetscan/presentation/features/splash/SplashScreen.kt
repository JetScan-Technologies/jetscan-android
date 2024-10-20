package io.github.dracula101.jetscan.presentation.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.dracula101.jetscan.BuildConfig
import io.github.dracula101.jetscan.R

@Composable
fun SplashScreen() {
    Surface (
        modifier = Modifier.fillMaxSize(),
    ){
        // Simple black background
        if(BuildConfig.DEBUG){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "Splash Screen",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}