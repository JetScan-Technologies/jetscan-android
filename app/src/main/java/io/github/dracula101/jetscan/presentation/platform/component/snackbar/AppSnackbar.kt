package io.github.dracula101.jetscan.presentation.platform.component.snackbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import timber.log.Timber

data class WarningSnackbar(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    val details: String? = null,
    override val withDismissAction: Boolean
) : SnackbarVisuals

data class ErrorSnackbar(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    val details: String? = null,
    val errorCode: String? = null,
    override val withDismissAction: Boolean
) : SnackbarVisuals

data class SuccessSnackbar(
    override val duration: SnackbarDuration,
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = true,
) : SnackbarVisuals

const val DETAIL_ALPHA = 0.5f
val ICON_TEXT_SPACING = 16.dp

@Composable
fun WarningSnackBar(
    message: String,
    details: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Snackbar(
        containerColor = containerColor,
        modifier = Modifier
            .padding(WindowInsets.ime.asPaddingValues())
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                clip = false
            )

    ) {
        CompositionLocalProvider {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(ICON_TEXT_SPACING))
                Column  {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (details != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            details,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = DETAIL_ALPHA),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorSnackBar(
    message: String,
    details: String? = null,
    errorCode: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Snackbar(
        containerColor = containerColor,
        modifier = Modifier
            .padding(WindowInsets.ime.asPaddingValues())
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                clip = false
            )
    ) {
        CompositionLocalProvider {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(ICON_TEXT_SPACING))
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (details != null) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                details,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = DETAIL_ALPHA),
                            )
                            if (errorCode != null) {
                                Text(
                                    " - ",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = DETAIL_ALPHA),
                                )
                                Text(
                                    text = errorCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clip(MaterialTheme.shapes.small)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SuccessSnackBar(
    message: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Snackbar(
        containerColor = containerColor,
        modifier = Modifier
            .padding(WindowInsets.ime.asPaddingValues())
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                clip = false
            )
    ) {
        CompositionLocalProvider {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                )
                Spacer(modifier = Modifier.width(ICON_TEXT_SPACING))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}



@Composable
fun AppSnackBar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = snackbarData.visuals
    when (visuals) {
        is WarningSnackbar -> {
            WarningSnackBar(
                message = visuals.message,
                details = visuals.details,
            )
        }
        is ErrorSnackbar -> {
            ErrorSnackBar(
                message = visuals.message,
                details = visuals.details,
                errorCode = visuals.errorCode,
            )
        }
        is SuccessSnackbar -> {
            SuccessSnackBar(
                message = visuals.message
            )
        }
        else -> {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = modifier,
                snackbarData = snackbarData
            )
        }
    }
}


@Preview
@Composable
fun WarningSnackbarPreview() {
    WarningSnackBar(
        message = "Warning message",
        details = "Warning details are a bit too long for this preview. Please check the app for the full message."
    )
}

@Preview
@Composable
fun ErrorSnackbarPreview() {
    ErrorSnackBar(
        message = "Error message",
        details = "Error details",
        errorCode = "Error code"
    )
}

@Preview
@Composable
fun SuccessSnackbarPreview() {
    SuccessSnackBar(
        message = "Success message"
    )
}