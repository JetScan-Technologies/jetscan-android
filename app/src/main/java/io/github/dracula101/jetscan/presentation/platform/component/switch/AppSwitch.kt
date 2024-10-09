package io.github.dracula101.jetscan.presentation.platform.component.switch

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.features.home.settings_view.SettingsAction

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(24.dp)
            .scale(0.8f)
    )
}