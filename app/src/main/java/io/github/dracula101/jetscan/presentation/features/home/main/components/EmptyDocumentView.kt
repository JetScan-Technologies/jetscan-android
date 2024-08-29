package io.github.dracula101.jetscan.presentation.features.home.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.R

@Composable
fun EmptyDocumentView(
    modifier : Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_documents_bg),
                contentDescription = "No Documents",
                modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Documents Scanned",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Your recently scanned documents can be seen here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}