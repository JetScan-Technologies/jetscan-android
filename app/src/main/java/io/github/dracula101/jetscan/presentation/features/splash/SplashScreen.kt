package io.github.dracula101.jetscan.presentation.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.dracula101.jetscan.R

@Composable
fun SplashScreen() {
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "App Logo",
            modifier = Modifier.fillMaxSize(0.4f)
        )
    }
}