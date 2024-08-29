package io.github.dracula101.jetscan.presentation.platform.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.TwoTone.MoreVert,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "",
        )
    }
}
