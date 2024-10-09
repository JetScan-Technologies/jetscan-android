package io.github.dracula101.jetscan.presentation.features.settings.document.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.AppDropDown
import io.github.dracula101.jetscan.presentation.platform.component.dropdown.MenuItem
import io.github.dracula101.jetscan.presentation.platform.component.switch.AppSwitch
import io.github.dracula101.jetscan.presentation.platform.feature.app.utils.debugBorder

@Composable
fun SettingListTile(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    leading: (@Composable () -> Unit)? = null,
    menuItems: List<MenuItem> = emptyList(),
    currentItem: String,
    enabled: Boolean = true,
    hideDivider: Boolean = false,
) {
    val isMenuOpen = remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 2,
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box{
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = enabled) {
                            isMenuOpen.value = true
                        }
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ){
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentItem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = "Down Arrow",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
                AppDropDown(
                    expanded = isMenuOpen.value,
                    offset = DpOffset(0.dp, 10.dp),
                    onDismissRequest = {
                        isMenuOpen.value = false
                    },
                    modifier = Modifier
                        .width(120.dp),
                    items = menuItems,
                )
            }
        }
        if (!hideDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            )
        }
    }
}