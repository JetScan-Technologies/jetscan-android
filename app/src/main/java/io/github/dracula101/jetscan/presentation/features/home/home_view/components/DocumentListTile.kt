package io.github.dracula101.jetscan.presentation.features.home.home_view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.extensions.gradientContainer

@Composable
fun DocumentsListTitle(
    onDocumentAdd: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.Transparent
                    ),
                    startY = 120f
                )
            )
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent Documents",
            style = MaterialTheme.typography.titleLarge,
        )
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onDocumentAdd)
                .gradientContainer()
                .padding(
                    horizontal = 16.dp,
                    vertical = 6.dp
                )

        ) {
            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
