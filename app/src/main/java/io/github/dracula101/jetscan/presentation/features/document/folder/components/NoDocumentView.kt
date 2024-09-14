package io.github.dracula101.jetscan.presentation.features.document.folder.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.platform.component.button.GradientButton


@Composable
fun NoDocumentView(
    topPadding: Dp = 120.dp,
    onDocumentAdd: () -> Unit = {},
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.size(topPadding))
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = "Document",
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "No documents found",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            "Try adding a document to this folder",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.size(16.dp))
        // GradientButton(
        //     text = "Add Document",
        //     onClick = onDocumentAdd
        // )
        // Spacer(modifier = Modifier.size(48.dp))
    }
}