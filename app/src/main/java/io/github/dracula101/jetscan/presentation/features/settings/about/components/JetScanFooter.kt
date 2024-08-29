package io.github.dracula101.jetscan.presentation.features.settings.about.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dracula101.jetscan.R

@Composable
fun JetScanFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp)
    ) {
        Text(
            text = "JetScan",
            fontSize = 52.sp,
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
        )
        Text(
            text = "Made with ❤️ by Dracula",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
        )
        Spacer(modifier = Modifier.size(120.dp))
    }
}