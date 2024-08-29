package io.github.dracula101.jetscan.presentation.features.document.scanner.components

import androidx.camera.core.AspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.features.document.scanner.ScannerState
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.camera.CameraScannedImage


@Composable
fun CameraFunctions(
    modifier: Modifier = Modifier,
    state: ScannerState,
    previewAspectRatio: Float,
    document: CameraScannedImage?,
    isCapturingPhoto: Boolean,
    onCapturePhoto: () -> Unit,
    onDocumentClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        IconButton (
            modifier = Modifier
                .align(Alignment.CenterStart),
            onClick = { onDocumentClick() }
        ){
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = "Document",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(32.dp)
                )
            }
        }
        CameraClickButton(
            modifier = Modifier.align(Alignment.Center),
            onClick = { onCapturePhoto() },
            cameraRotation = state.cameraRotationValue,
            isCapturingPhoto = isCapturingPhoto,
            scannerTab = state.documentType,
        )
        PreviewImage(
            aspectRatio = previewAspectRatio,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .heightIn(min = 50.dp, max = 80.dp)
                .fillMaxHeight(),
            document = document,
        ) {
            onDocumentClick()
        }
    }
}
