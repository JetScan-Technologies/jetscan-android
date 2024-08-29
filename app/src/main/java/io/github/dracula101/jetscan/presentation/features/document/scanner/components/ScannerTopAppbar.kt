package io.github.dracula101.jetscan.presentation.features.document.scanner.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraRear
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.GridOff
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.appbar.BackButtonIcon
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.FlashMode

@Composable
fun ScannerTopAppBar(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    flashModeArgs: FlashModeArgs,
    gridModeArgs: GridModeArgs,
    cameraFacingArgs: CameraFacingArgs
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onNavigateBack() }
        ){
            BackButtonIcon()
        }
        FlashModeButton(
            flashMode = flashModeArgs.flashMode,
            onFlashModeChanged = flashModeArgs.onFlashModeChanged,
            enabled = flashModeArgs.enabled
        )
        GridModeButton(
            gridMode = gridModeArgs.gridModeOn,
            onGridModeChanged = gridModeArgs.onGridModeChanged,
            enabled = gridModeArgs.enabled
        )
        CameraFacingButton(
            cameraFacing = cameraFacingArgs.cameraFacingBack,
            onCameraFacingChanged = cameraFacingArgs.onCameraFacingChanged,
            enabled = cameraFacingArgs.enabled
        )
    }
}

@Composable
fun FlashModeButton(
    flashMode: FlashMode,
    onFlashModeChanged: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onFlashModeChanged,
        modifier = Modifier
            .padding(
                horizontal = 4.dp,
            ),
        enabled = enabled
    ) {
        Icon(
            imageVector = when (flashMode) {
                FlashMode.ON -> Icons.Rounded.FlashOn
                FlashMode.OFF -> Icons.Rounded.FlashOff
                FlashMode.AUTO -> Icons.Rounded.FlashAuto
                FlashMode.TORCH -> Icons.Rounded.FlashlightOn
            },
            contentDescription = "Flash mode",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GridModeButton(
    gridMode: Boolean,
    onGridModeChanged: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onGridModeChanged,
        modifier = Modifier
            .padding(
                horizontal = 4.dp,
            ),
        enabled = enabled
    ) {
        Icon(
            imageVector = when (gridMode) {
                true -> Icons.Rounded.GridOn
                false -> Icons.Rounded.GridOff
            },
            contentDescription = "Grid mode",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CameraFacingButton(
    cameraFacing: Boolean,
    onCameraFacingChanged: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onCameraFacingChanged,
        modifier = Modifier
            .padding(
                horizontal = 4.dp,
            ),
        enabled = enabled
    ) {
        Icon(
            imageVector = when (cameraFacing) {
                true -> Icons.Rounded.FlipCameraIos
                false -> Icons.Rounded.CameraRear
            },
            contentDescription = "Camera facing",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}


data class FlashModeArgs(
    val flashMode: FlashMode,
    val onFlashModeChanged: () -> Unit,
    val enabled: Boolean = true
)

data class GridModeArgs(
    val gridModeOn: Boolean,
    val onGridModeChanged: () -> Unit,
    val enabled: Boolean = true
)

data class CameraFacingArgs(
    val cameraFacingBack: Boolean,
    val onCameraFacingChanged: () -> Unit,
    val enabled: Boolean = true
)