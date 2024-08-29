package io.github.dracula101.jetscan.presentation.features.settings.about.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import io.github.dracula101.jetscan.BuildConfig


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun DeveloperInfoBottomSheet(
    onDismiss: () -> Unit
){
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = "Who made JetScan?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Made and maintained by Dracula101",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    append("If you are willing to contribute, please reach out to us at ")
                    append("info.jetscan@gmail.com")
                    this.addStyle(
                        MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline).toSpanStyle(),
                        60,
                        82
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:info.jetscan@gmail.com")
                    }
                    startActivity(context, emailIntent, null)
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Text(
                "Version: ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}