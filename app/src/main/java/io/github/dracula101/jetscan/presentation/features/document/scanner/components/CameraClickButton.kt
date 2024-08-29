package io.github.dracula101.jetscan.presentation.features.document.scanner.components


import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.document.DocumentType
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun CameraClickButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cameraRotation: Float = 0f,
    height: Float = 70f,
    isCapturingPhoto: Boolean = false,
    scannerTab: DocumentType
) {
    val photoCapturedState = remember { mutableStateOf(isCapturingPhoto) }
    LaunchedEffect(isCapturingPhoto) {
        photoCapturedState.value = isCapturingPhoto
    }
    val shouldShowProgressAnimation = animateFloatAsState(
        targetValue = if (photoCapturedState.value) 1f else 0f,
        label = "Progress Button Animation"
    )
    val rotation = remember {
        Animatable(0f)
    }
    LaunchedEffect(cameraRotation) {
        rotation.animateTo(
            targetValue = cameraRotation,
            animationSpec = tween(durationMillis = 500)
        )

    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        this.constraints
        Box(
            modifier = Modifier
                .size(height.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable {
                    if (scannerTab != DocumentType.QR_CODE && scannerTab != DocumentType.BAR_CODE) onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = shouldShowProgressAnimation.value
                    }
                    .requiredSize(height.dp - 10.dp),
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.surface
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1 - shouldShowProgressAnimation.value
                    }
                    .requiredSize(height.dp - 10.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
            when(scannerTab){
                DocumentType.DOCUMENT -> {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(28.dp)
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = -rotation.value
                            }
                    )
                }
                DocumentType.QR_CODE, DocumentType.BAR_CODE -> {
                    Icon(
                        painter = painterResource(
                            if (scannerTab == DocumentType.QR_CODE) R.drawable.qr_code else R.drawable.barcode
                        ),
                        contentDescription = "QR Code",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> {}
            }
        }
    }
}