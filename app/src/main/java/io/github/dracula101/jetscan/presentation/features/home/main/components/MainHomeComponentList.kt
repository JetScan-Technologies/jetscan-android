package io.github.dracula101.jetscan.presentation.features.home.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder

@Composable
fun MainHomeComponentList(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MainHomeExpandedComponentList(
    icon: Painter,
    title: String,
    color: Color,
    modifier : Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column (
        modifier = Modifier
            .padding(
                start = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() / 2
            )
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            )

    ){
        Row (
            modifier = modifier
                .padding(8.dp)
                .padding(
                    start = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() / 3
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth(0.15f)
                    .background(color.copy(alpha = 0.2f))
                    .padding(8.dp)
            ){
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                )
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
            )
        }
    }
}
