package io.github.dracula101.jetscan.presentation.features.settings.about.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.BuildConfig
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