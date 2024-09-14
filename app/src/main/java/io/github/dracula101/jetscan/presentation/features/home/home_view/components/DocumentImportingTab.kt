package io.github.dracula101.jetscan.presentation.features.home.home_view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dracula101.jetscan.presentation.features.home.main.MainHomeAction
import io.github.dracula101.jetscan.presentation.platform.base.ImportDocumentState
import io.github.dracula101.jetscan.presentation.platform.component.button.CircleButton
import io.github.dracula101.jetscan.presentation.platform.component.loader.AnimatedLoader

@Composable
fun DocumentImportingState(
    modifier: Modifier = Modifier,
    importDocumentState: ImportDocumentState,
    // onCancel: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, bottom = 8.dp, end = 16.dp)
    ) {
        Icon(
            when (importDocumentState) {
                is ImportDocumentState.InProgress -> Icons.Rounded.FileDownload
                is ImportDocumentState.Success -> Icons.Rounded.CheckCircleOutline
                else -> Icons.Rounded.Close
            },
            contentDescription = "Import Icon",
            modifier = Modifier
                .size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = when (importDocumentState) {
                        is ImportDocumentState.InProgress -> "Importing Document - ${importDocumentState.fileName}"
                        is ImportDocumentState.Success -> "Document Imported"
                        is ImportDocumentState.Error -> "Error Importing Document"
                        is ImportDocumentState.Started -> "Importing Document"
                        else -> "---"
                    },
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                // if (importDocumentState is MainHomeAction.ImportDocumentState.InProgress) {
                //     CircleButton(
                //         imageVector = Icons.Rounded.Close,
                //         onClick = onCancel
                //     )
                // }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (importDocumentState) {
                    is ImportDocumentState.InProgress -> {
                        AnimatedLoader(
                            value = importDocumentState.currentProgress / importDocumentState.totalProgress,
                            modifier = Modifier
                                .fillMaxWidth(0.85f),
                            height = 12.dp,
                        )
                        Text(
                            "${
                                (importDocumentState.currentProgress / importDocumentState.totalProgress).times(
                                    100f
                                ).toInt()
                            }%",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    is ImportDocumentState.Error -> {
                        Text(
                            text = "Error Importing Document",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
