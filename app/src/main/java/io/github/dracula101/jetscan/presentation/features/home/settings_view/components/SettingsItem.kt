package io.github.dracula101.jetscan.presentation.features.home.settings_view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SettingsItem(
    title: String,
    icon: Any? = null,
    color: Color? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true
){
    Column (
        modifier = Modifier
            .then(
                if(onClick != null) {
                    Modifier.clickable {
                        onClick()
                    }
                }else {
                    Modifier
                }
            )
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when(icon){
                    is Int -> {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = "Icon",
                            modifier = Modifier.size(24.dp),
                            colorFilter = color?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) }
                        )
                    }
                    is ImageVector -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Icon",
                            modifier = Modifier.size(24.dp),
                            tint = color ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    is String -> {
                        AsyncImage(
                            model = icon,
                            contentDescription = "Icon",
                            modifier = Modifier.size(24.dp),
                            colorFilter = color?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color ?: MaterialTheme.colorScheme.onSurface,
                )
            }
            if(trailing != null) {
                trailing()
            }else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Navigate",
                    modifier = Modifier
                        .size(20.dp),
                    tint = color ?: MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if(showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}