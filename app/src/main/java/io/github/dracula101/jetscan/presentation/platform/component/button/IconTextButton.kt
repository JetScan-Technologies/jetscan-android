package io.github.dracula101.jetscan.presentation.platform.component.button


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    icon: ImageVector,
    iconSize: Int = 28,
    rippleSize: Int = 40,
    onClick: () -> Unit,
    spacing: PaddingValues = PaddingValues(4.dp),
) {
    val indication = rememberRipple(radius = rippleSize.dp, bounded = false)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                role = Role.Button,
                onClick = onClick,
            )
            .widthIn(min = 42.dp, max = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(iconSize.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(
            modifier = Modifier
                .padding(spacing)
        )
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}