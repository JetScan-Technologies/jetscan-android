package io.github.dracula101.jetscan.presentation.platform.component.checkbox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.clip(CircleShape)
    )
}

@Composable
fun CircleCheckbox(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    onChecked: (Boolean) -> Unit,
) {
    val color = MaterialTheme.colorScheme
    val imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
    val tint = if (selected) color.primary.copy(alpha = 0.8f) else color.onSurface.copy(alpha = 0.8f)
    val background : Color = if (selected) Color.White else Color.Transparent
    val isSelected by remember { mutableStateOf(selected) }
    IconButton(
        onClick = {
            onChecked(!isSelected)
        },
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = imageVector,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .background(background, shape = CircleShape),
            contentDescription = "checkbox"
        )
    }
}