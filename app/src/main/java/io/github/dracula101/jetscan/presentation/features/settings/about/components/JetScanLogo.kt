package io.github.dracula101.jetscan.presentation.features.settings.about.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.dracula101.jetscan.R

@Composable
fun JetScanLogo(
    modifier: Modifier = Modifier,
){
    Image(
        painter = painterResource(id = R.drawable.app_icon_foreground),
        contentDescription = "JetScan Logo",
        modifier = modifier
    )
}