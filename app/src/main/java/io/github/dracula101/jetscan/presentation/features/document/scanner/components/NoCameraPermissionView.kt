package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.features.document.scanner.openAppSettings


@Composable
fun NoPermissionView(
    modifier: Modifier = Modifier,
    onPermissionRequested: () -> Unit = {},
    onNavigateBack: ()-> Unit = {},
    isPermissionDeclined: Boolean?,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ){
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ){
            TextButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.padding(8.dp)
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "Back Navigation",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Camera Access",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(100.dp)
                    .alpha(0.5f)
            )
            Text(
                text = "Camera Access",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = if (isPermissionDeclined == true) {
                    "Allow camera permission from the settings to scan documents"
                } else {
                    "Camera permission is required to scan documents"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(20.dp))
            ElevatedButton(
                onClick = {
                    if (isPermissionDeclined == true) {
                        context.openAppSettings()
                    } else {
                        onPermissionRequested()
                    }
                },
            ) {
                Text(text = "Allow")
            }
        }
    }
}